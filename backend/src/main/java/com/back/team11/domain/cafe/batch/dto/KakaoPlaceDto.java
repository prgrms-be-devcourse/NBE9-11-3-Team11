package com.back.team11.domain.cafe.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPlaceDto(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("road_address_name") String roadAddressName,
        @JsonProperty("address_name") String addressName,
        String phone,
        String x,
        String y
) {
}
