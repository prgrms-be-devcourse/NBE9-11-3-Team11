package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.dto.CafeBaseInfo.Companion.from
import com.back.team11.domain.cafe.entity.Cafe
import java.time.LocalDateTime

// 카페 목록/마커용 응답 DTO
data class CafeListResponse(
    val cafe: CafeBaseInfo,
    val wishlistCount: Long,
    val createdAt: LocalDateTime? // 추가
) {
    companion object {
        // Cafe 엔티티와 찜 수로부터 CafeListResponse 생성
        @JvmStatic
        fun from(cafe: Cafe, wishlistCount: Long): CafeListResponse {
            return CafeListResponse(
                cafe = CafeBaseInfo.from(cafe),
                wishlistCount = wishlistCount,
                createdAt = cafe.createdAt // 추가
            )
        }
    }
}
