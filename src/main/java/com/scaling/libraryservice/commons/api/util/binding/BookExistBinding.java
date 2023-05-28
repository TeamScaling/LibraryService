package com.scaling.libraryservice.commons.api.util.binding;

import com.scaling.libraryservice.mapBook.dto.ApiBookExistDto;
import com.scaling.libraryservice.mapBook.exception.OpenApiException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
public class BookExistBinding extends Data4LibraryBinding implements
    BindingStrategy<ApiBookExistDto> {

    /**
     * API 응답 데이터를 ApiBookExistDto로 변환합니다.
     *
     * @param apiResponse API 응답 데이터
     * @return 변환된 ApiBookExistDto 객체
     * @throws OpenApiException API 응답을 처리하는 도중 에러가 발생하면 예외를 던집니다.
     */
    @Override
    public ApiBookExistDto bind(ResponseEntity<String> apiResponse) throws OpenApiException {
        if (apiResponse == null) {
            return null;
        }

        JSONObject respJsonObj = getJsonObjFromResponse(apiResponse);

        return new ApiBookExistDto(
            respJsonObj.getJSONObject("request"),
            respJsonObj.getJSONObject("result"));
    }


}
