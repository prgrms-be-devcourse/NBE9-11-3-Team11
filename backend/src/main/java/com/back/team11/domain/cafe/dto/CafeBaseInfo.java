package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.*;
import java.math.BigDecimal;

// 카페 공통 기본 정보 — 모든 Response DTO에서 공유하는 16개 필드
public record CafeBaseInfo(
        Long cafeId,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String description,
        CafeType type,
        Franchise franchise,
        Boolean hasToilet,
        Boolean hasOutlet,
        Boolean hasWifi,
        FloorCount floorCount,
        Boolean hasSeparateSpace,
        CongestionLevel congestionLevel,
        String imageUrl
) {
    // Cafe 엔티티로부터 CafeBaseInfo 생성
    public static CafeBaseInfo from(Cafe cafe) {
        return new CafeBaseInfo(
                cafe.getId(),
                cafe.getName(),
                cafe.getAddress(),
                cafe.getLatitude(),
                cafe.getLongitude(),
                cafe.getPhone(),
                cafe.getDescription(),
                cafe.getType(),
                cafe.getFranchise(),
                cafe.getHasToilet(),
                cafe.getHasOutlet(),
                cafe.getHasWifi(),
                cafe.getFloorCount(),
                cafe.getHasSeparateSpace(),
                cafe.getCongestionLevel(),
                cafe.getImageUrl()
        );
    }
}
