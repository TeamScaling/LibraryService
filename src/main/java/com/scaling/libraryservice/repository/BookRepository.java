package com.scaling.libraryservice.repository;

import com.scaling.libraryservice.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    // 제목검색 FULLTEXT 서치 이용
//    @Query(value = "SELECT * FROM books WHERE MATCH(TITLE_NM) AGAINST (:query IN BOOLEAN MODE)", nativeQuery = true)
//    List<Book> findBooksByTitlePage(@Param("query") String query);

    // 작가검색 FULLTEXT 서치 이용 + 페이징
    @Query(value = "SELECT * FROM books WHERE MATCH(AUTHR_NM) AGAINST (:query IN BOOLEAN MODE)", nativeQuery = true)
    Page<Book> findBooksByAuthor(@Param("query") String query, Pageable pageable);

    // 제목검색 FULLTEXT 서치 이용 + 페이징
//    @Query(value = "SELECT * FROM library_service.books WHERE MATCH(TITLE_NM) AGAINST (:query IN BOOLEAN MODE)",
//        countQuery = "SELECT COUNT(*) FROM books WHERE MATCH(TITLE_NM) AGAINST (:query)",nativeQuery = true)
//    Page<Book> findBooksByTitlePage(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM books WHERE MATCH(ENG_TITILE_NM) AGAINST (:query IN BOOLEAN MODE)",
        countQuery = "SELECT COUNT(*) FROM books WHERE MATCH(TITLE_NM) AGAINST (:query)",nativeQuery = true)
    Page<Book> findBooksByEngilshTitlePage(String query, Pageable pageable);

    @Query(value = "SELECT * FROM library_service.books WHERE MATCH(TITLE_NM) AGAINST (:query)",
        countQuery = "SELECT COUNT(*) FROM books WHERE MATCH(TITLE_NM) AGAINST (:query)",nativeQuery = true)
    Page<Book> findBooksByTitlePage(@Param("query") String query, Pageable pageable);
}

