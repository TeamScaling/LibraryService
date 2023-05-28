package com.scaling.libraryservice.commons.api.apiConnection;

import com.scaling.libraryservice.mapBook.domain.ApiConnection;
import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
public class KakaoBookConn implements ApiConnection {

    private Long id;
    private static final String API_URL = "https://dapi.kakao.com/v3/search/book";

    private static final String DEFAULT_AUTH_KEY = "KakaoAK e3354f2e73c173cb2d0420123c89c961";

    private final String target;

    public KakaoBookConn(String target) {
        this.target = target;
    }

    public KakaoBookConn(String target,Long id) {
        this.id = id;
        this.target = target;
    }

    @Override
    public UriComponentsBuilder configUriBuilder() {

        return UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam("query", target);
    }
}
