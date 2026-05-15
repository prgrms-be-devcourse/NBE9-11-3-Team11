package com.back.team11.domain.cafe.batch.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoSearchResponse(
    val documents: List<KakaoPlaceDto>,
    val meta: Meta,
) {
    data class Meta(
        @JsonProperty("is_end") val isEnd: Boolean,
    )
}