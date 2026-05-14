package com.back.team11.domain.cafe.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoSearchResponse(
        List<KakaoPlaceDto> documents,
        Meta meta
) {
    public record Meta(
            @JsonProperty("is_end") boolean isEnd
    ){}
}
