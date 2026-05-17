package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.entity.*
import java.math.BigDecimal

// 카페 공통 기본 정보 — 모든 Response DTO에서 공유하는 16개 필드
data class CafeBaseInfo(
    val cafeId: Long,
    val name: String,
    val address: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val phone: String?,
    val description: String?,
    val type: CafeType,
    val franchise: Franchise,
    val hasToilet: Boolean,
    val hasOutlet: Boolean,
    val hasWifi: Boolean,
    val floorCount: FloorCount,
    val hasSeparateSpace: Boolean,
    val congestionLevel: CongestionLevel,
    val imageUrl: String?,
) {
    companion object {
        // Cafe 엔티티로부터 CafeBaseInfo 생성
        fun from(cafe: Cafe): CafeBaseInfo = CafeBaseInfo(
            cafeId = cafe.id,
            name = cafe.name,
            address = cafe.address,
            latitude = cafe.latitude,
            longitude = cafe.longitude,
            phone = cafe.phone,
            description = cafe.description,
            type = cafe.type,
            franchise = cafe.franchise,
            hasToilet = cafe.hasToilet,
            hasOutlet = cafe.hasOutlet,
            hasWifi = cafe.hasWifi,
            floorCount = cafe.floorCount,
            hasSeparateSpace = cafe.hasSeparateSpace,
            congestionLevel = cafe.congestionLevel,
            imageUrl = cafe.imageUrl,
        )
    }
}