package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.cafe.entity.CafeStatus
import java.time.LocalDateTime

// 관리자용 응답 DTO — 사용자용 CafeResponse와 달리 status, createdAt 포함
data class AdminCafeResponse(
    val cafe: CafeBaseInfo,
    val status: CafeStatus,  // 관리자만 확인 필요
    val createdAt: LocalDateTime? // 관리자만 확인 필요
) {
    companion object {
        // Cafe 엔티티로부터 AdminCafeResponse 생성
        fun from(cafe: Cafe): AdminCafeResponse = AdminCafeResponse(
            cafe = CafeBaseInfo.from(cafe),
            status = cafe.status,
            createdAt = cafe.createdAt,
        )
    }
}
