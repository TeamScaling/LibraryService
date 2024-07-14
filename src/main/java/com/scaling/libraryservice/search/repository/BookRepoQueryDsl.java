package com.scaling.libraryservice.search.repository;

import static com.scaling.libraryservice.search.engine.SearchMode.BOOLEAN_MODE;
import static com.scaling.libraryservice.search.engine.TitleType.TOKEN_ALL_ETC;
import static com.scaling.libraryservice.search.engine.TitleType.TOKEN_COMPLEX;
import static com.scaling.libraryservice.search.entity.QBook.book;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.scaling.libraryservice.search.dto.BookDto;
import com.scaling.libraryservice.search.engine.SearchMode;
import com.scaling.libraryservice.search.engine.TitleQuery;
import com.scaling.libraryservice.search.entity.Book;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
@Slf4j
public class BookRepoQueryDsl {

    private final JPAQueryFactory factory;

    private final static int LIMIT_CNT = 100;

    private final static double DEFAULT_SCORE_OF_MATCH = 0.0;

    @Transactional(readOnly = true)
    public Page<BookDto> findBooks(TitleQuery titleQuery, Pageable pageable) {
        // match..against 문을 활용하여 Full text search를 수행
        JPAQuery<Book> books = getFtSearchJPAQuery(titleQuery, pageable);

        log.info(titleQuery.toString());

        // 최종적으로 페이징 처리된 도서 검색 결과를 반환.
        return PageableExecutionUtils.getPage(
            books.fetch()
                .stream()
                .map(BookDto::new)
                .collect(Collectors.toList()),
            pageable,
            () -> LIMIT_CNT
        );
    }

    @Transactional(readOnly = true)
    public BookDto findBooksByIsbn(String isbn) {
        Optional<Book> optBook = getNullableBookByIsbn(isbn);
        return optBook.map(BookDto::new)
            .orElseGet(BookDto::emptyDto);
    }

    private Optional<Book> getNullableBookByIsbn(String isbn) {
        return Optional.ofNullable(
            factory
                .selectFrom(book)
                .where(book.isbn.eq(isbn))
                .fetchOne()
        );
    }

    private JPAQuery<Book> getFtSearchJPAQuery(TitleQuery titleQuery, Pageable pageable) {
        BooleanBuilder builder = configBuilder(titleQuery);
        return factory.selectFrom(book)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());
    }

    private BooleanBuilder configBuilder(TitleQuery titleQuery) {
        BooleanBuilder builder = new BooleanBuilder();
        SearchMode mode = titleQuery.getTitleType().getMode();

        if (titleQuery.getTitleType() == TOKEN_ALL_ETC) {
            addFullTextSearchQuery(builder, mode, titleQuery.getEtcToken(), book.title);
        } else {
            addFullTextSearchQuery(builder, mode, titleQuery.getNnToken(), book.titleToken);
            // 명사와 나머지 단어들도 함께 찾아야 하면 title에 대한 Full Text 구문을 추가 한다.
            if (titleQuery.getTitleType() == TOKEN_COMPLEX) {
                addFullTextSearchQuery(builder, titleQuery.getTitleType().getSecondMode(),
                    titleQuery.getEtcToken(), book.title);
            }
        }
        return builder;
    }

    private void addFullTextSearchQuery(BooleanBuilder builder, SearchMode mode,
        String token, StringPath colum) {

        builder.and(
            getTemplate(
                mode,
                token,
                colum
            ).gt(
                DEFAULT_SCORE_OF_MATCH
            )
        );
    }

    // 사용자가 입력한 제목 쿼리를 분석한 결과를 바탕으로 boolean or natural 모드를 동적으로 선택
    NumberTemplate<Double> getTemplate(SearchMode mode, String token, StringPath colum) {
        String function;
        if (mode == BOOLEAN_MODE) {
            function = "function('BooleanMatch',{0},{1})";
            // boolean 모드에서 모두 반드시 포함된 결과를 위해 '+'를 붙여주는 정적 메소드 호출.
            token = splitAddPlusSign(token);
        } else {
            function = "function('NaturalMatch',{0},{1})";
        }
        return Expressions.numberTemplate(Double.class, function, colum, token);
    }

    // boolean mode를 위한 메소드
    private String splitAddPlusSign(@NonNull String target) {
        target = target.trim();
        return Arrays.stream(target.split(" "))
            .map(token -> "+" + token)
            .collect(Collectors.joining(" "));
    }

}
