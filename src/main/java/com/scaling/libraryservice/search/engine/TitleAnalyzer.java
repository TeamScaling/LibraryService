package com.scaling.libraryservice.search.engine;

import static com.scaling.libraryservice.search.engine.TitleType.*;
import static com.scaling.libraryservice.search.engine.Token.ETC_TOKEN;
import static com.scaling.libraryservice.search.engine.Token.NN_TOKEN;

import com.scaling.libraryservice.commons.timer.MeasureTaskTime;
import com.scaling.libraryservice.search.exception.NotQualifiedQueryException;
import com.scaling.libraryservice.search.engine.TitleQuery.TitleQueryBuilder;
import com.scaling.libraryservice.search.engine.filter.FilterStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 검색어에 대한 분석을 수행하는 클래스입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TitleAnalyzer {

    private final TitleTokenizer tokenizer;
    private final FilterStream filterStream;
    private static final int ETC_TOKEN_THRESHOLD_SIZE = 1;
    private static final int NN_TOKEN_THRESHOLD_SIZE = 2;

    @MeasureTaskTime
    public TitleQuery analyze(String query, boolean filterOn) throws NotQualifiedQueryException {

        // filterOn에 따라 filter를 진행 할지 결정 된다.
        query = filterStream.doFiltering(query, filterOn);

        // tokenizer를 거치면 원본 검색어가 수정되기 때문에 필요 모드에서 사용하기 위해 원본을 저장 한다.
        String originalQuery = query;

        AnalyzedResult analyzedResult = analyzeQuery(query);

        // title 분석에 대한 결과를 담을 TitleQuery의 builder를 만든다.
        TitleQueryBuilder titleQueryBuilder = setUpBuilder(analyzedResult, originalQuery);

        // 어떤 검색 전략를 실시 할지 결정 한다.
        return determineTitleType(analyzedResult, titleQueryBuilder);
    }

    private AnalyzedResult analyzeQuery(String query) {
        Map<Token, List<String>> titleMap = tokenizer.tokenize(query);

        String nnToken = String.join(" ",
            titleMap.getOrDefault(NN_TOKEN, Collections.emptyList())
        );

        String etcToken = String.join(" ",
            titleMap.getOrDefault(ETC_TOKEN, Collections.emptyList())
        );

        return new AnalyzedResult(nnToken, etcToken);
    }

    private TitleQuery determineTitleType(AnalyzedResult result,
        TitleQueryBuilder titleQueryBuilder) {
        if (result.getNnTokenCount() >= NN_TOKEN_THRESHOLD_SIZE) {
            return titleQueryBuilder.titleType(TOKEN_TWO_OR_MORE).build();
        } else if (result.getEtcTokenCount() >= ETC_TOKEN_THRESHOLD_SIZE) {
            return considerEtcToken(result, titleQueryBuilder);
        } else {
            return titleQueryBuilder.titleType(TOKEN_ONE).build();
        }
    }

    private TitleQueryBuilder setUpBuilder(AnalyzedResult result, String originalQuery) {
        return new TitleQueryBuilder()
            .userQuery(originalQuery)
            .nnToken(result.nnToken)
            .etcToken(result.etcToken);
    }

    private TitleQuery considerEtcToken(AnalyzedResult result,
        TitleQueryBuilder titleQueryBuilder) {
        return result.getNnTokenCount() == 0 ? titleQueryBuilder.titleType(TOKEN_ALL_ETC)
            .etcToken(titleQueryBuilder.getUserQuery())
            .build()
            : titleQueryBuilder.titleType(TOKEN_COMPLEX).build();
    }

    private record AnalyzedResult(String nnToken, String etcToken) {
        public int getNnTokenCount() {
            return nnToken.isBlank() ? 0 : nnToken.split(" ").length;
        }

        public int getEtcTokenCount() {
            return etcToken.isBlank() ? 0 : etcToken.split(" ").length;
        }
    }

}
