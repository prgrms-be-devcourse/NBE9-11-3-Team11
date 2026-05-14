package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.Cafe;
import java.time.LocalDateTime;

// 카페 목록/마커용 응답 DTO
public record CafeListResponse(
        CafeBaseInfo cafe,
        long wishlistCount,
        LocalDateTime createdAt // 추가
) {
    // Cafe 엔티티와 찜 수로부터 CafeListResponse 생성
    public static CafeListResponse from(Cafe cafe, long wishlistCount) {
        return new CafeListResponse(
                CafeBaseInfo.from(cafe),
                wishlistCount,
                cafe.getCreatedAt() // 추가
        );
    }
}
