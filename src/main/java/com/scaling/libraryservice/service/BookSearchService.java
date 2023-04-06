package com.scaling.libraryservice.service;

import com.scaling.libraryservice.dto.BookDto;
import com.scaling.libraryservice.dto.MetaDto;
import com.scaling.libraryservice.dto.RespBooksDto;
import com.scaling.libraryservice.repository.BookQueryRepository;
import com.scaling.libraryservice.util.Tokenizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//consider : DB에서 검색해서 가져 오는 속도를 측정하는 클래스가 있어야 하지 않을까?
//consider : 스프링이라는 책을 쳤으면, 스프링과 관련된 책이 최상단에 나와야 한다. 자동식 스프링클러 설비 핸드북이 나오는 건
// 알맞지 않다. -> 이 문제를 해결 할 수 있는 방법은 뭐가 있을까??
//consider : searchBook 이후 얻은 결과를 일단은 콘솔로 확인하고 싶어서 일단은 하드 코딩으로 넣어 놨지만, 중복 & 배포시 제거
// 해야 하는 문제를 가지고 있다. -> 이것을 해결할 방법은?

@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final BookQueryRepository bookQueryRepo;

    private final Tokenizer tokenizer;

    public RespBooksDto searchBook(String title){
        List<String> token = tokenizer.tokenize(title);

        List<BookDto> books = bookQueryRepo.findBooksByToken(token)
            .stream().map(BookDto::new).toList();

        return new RespBooksDto(new MetaDto(),books);
    }



}
