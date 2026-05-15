package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.entity.Cafe
import java.time.LocalDateTime

// 카페 제보 응답 DTO
data class CafeResponse(
    val cafe: CafeBaseInfo,
    val createdAt: LocalDateTime? // 제보일을 기준으로 날짜를 표시
) {
    companion object {
        // Cafe 엔티티로부터 CafeResponse 생성
        @JvmStatic
        fun from(cafe: Cafe): CafeResponse = CafeResponse(
            cafe = CafeBaseInfo.from(cafe),
            createdAt = cafe.createdAt, // 제보일을 기준으로 날짜를 표시
        )
    }
}
