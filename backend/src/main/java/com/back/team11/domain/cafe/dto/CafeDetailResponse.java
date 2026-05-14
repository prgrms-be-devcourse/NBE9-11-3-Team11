package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.Cafe;
import java.time.LocalDateTime;

// 카페 상세 조회용 응답 DTO — 찜 수, 찜 여부 포함
public record CafeDetailResponse(
        CafeBaseInfo cafe,
        long wishlistCount,
        boolean isWishlisted,
        LocalDateTime createdAt
) {
    // Cafe 엔티티, 찜 수, 찜 여부로부터 CafeDetailResponse 생성
    public static CafeDetailResponse from(Cafe cafe, long wishlistCount, boolean isWishlisted) {
        return new CafeDetailResponse(
                CafeBaseInfo.from(cafe),
                wishlistCount,
                isWishlisted,
                cafe.getCreatedAt()
        );
    }
}
