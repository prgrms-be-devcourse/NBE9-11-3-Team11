package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.dto.CafeBaseInfo.Companion.from
import com.back.team11.domain.cafe.entity.Cafe
import java.time.LocalDateTime

// 카페 상세 조회용 응답 DTO — 찜 수, 찜 여부 포함
data class CafeDetailResponse(
    val cafe: CafeBaseInfo,
    val wishlistCount: Long,
    val isWishlisted: Boolean,
    val createdAt: LocalDateTime?,
) {
    companion object {
        // Cafe 엔티티, 찜 수, 찜 여부로부터 CafeDetailResponse 생성
        fun from(cafe: Cafe, wishlistCount: Long, isWishlisted: Boolean): CafeDetailResponse =
            CafeDetailResponse(
                cafe = CafeBaseInfo.from(cafe),
                wishlistCount = wishlistCount,
                isWishlisted = isWishlisted,
                createdAt = cafe.createdAt,
            )
    }
}
