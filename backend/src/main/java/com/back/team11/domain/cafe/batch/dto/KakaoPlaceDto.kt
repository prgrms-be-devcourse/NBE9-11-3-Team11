package com.back.team11.domain.cafe.batch.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoPlaceDto(
    val id: String,
    @JsonProperty("place_name") val placeName: String,
    @JsonProperty("road_address_name") val roadAddressName: String,
    @JsonProperty("address_name") val addressName: String,
    val phone: String,
    val x: String,
    val y: String,
)
