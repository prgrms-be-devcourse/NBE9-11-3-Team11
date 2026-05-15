package com.back.team11.domain.cafe.repository

import com.back.team11.domain.cafe.entity.CafeType
import com.back.team11.domain.cafe.entity.CongestionLevel
import com.back.team11.domain.cafe.entity.FloorCount
import com.back.team11.domain.cafe.entity.Franchise
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import lombok.Getter
import lombok.Setter

data class CafeSearchCondition(
    // 좌표 범위
    @field:DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 사이여야 합니다.")
    @field:DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 사이여야 합니다.")
    val swLat: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 사이여야 합니다.")
    @field:DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 사이여야 합니다.")
    val swLng: Double? = null,

    @field:DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 사이여야 합니다.")
    @field:DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 사이여야 합니다.")
    val neLat: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 사이여야 합니다.")
    @field:DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 사이여야 합니다.")
    val neLng: Double? = null,

    // 필터링
    val type: CafeType? = null,
    val franchises: List<Franchise>? = null,         // 다중 선택
    val hasToilet: Boolean? = null,
    val hasOutlet: Boolean? = null,
    val hasWifi: Boolean? = null,
    val floorCounts: List<FloorCount>? = null,       // 다중 선택
    val hasSeparateSpace: Boolean? = null,
    val congestionLevels: List<CongestionLevel>? = null, // 다중 선택
)
