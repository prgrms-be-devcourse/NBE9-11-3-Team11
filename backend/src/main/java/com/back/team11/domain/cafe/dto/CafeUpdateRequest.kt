package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.entity.CafeType
import com.back.team11.domain.cafe.entity.CongestionLevel
import com.back.team11.domain.cafe.entity.FloorCount
import com.back.team11.domain.cafe.entity.Franchise
import lombok.Getter
import lombok.NoArgsConstructor
import java.math.BigDecimal

// PATCH 방식 - 전송된 필드만 수정, null인 필드는 기존값 유지
data class CafeUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val phone: String? = null,
    val description: String? = null,
    val type: CafeType? = null,
    val franchise: Franchise? = null,
    val hasToilet: Boolean? = null,
    val hasOutlet: Boolean? = null,
    val hasWifi: Boolean? = null,
    val floorCount: FloorCount? = null,
    val hasSeparateSpace: Boolean? = null,
    val congestionLevel: CongestionLevel? = null,
    val imageUrl: String? = null,
)
