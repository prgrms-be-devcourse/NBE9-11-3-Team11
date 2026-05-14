package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.Cafe;
import java.time.LocalDateTime;

// 카페 제보 응답 DTO
public record CafeResponse(
        CafeBaseInfo cafe,
        LocalDateTime createdAt // 제보일을 기준으로 날짜를 표시
) {
    // Cafe 엔티티로부터 CafeResponse 생성
    public static CafeResponse from(Cafe cafe) {
        return new CafeResponse(
                CafeBaseInfo.from(cafe),
                cafe.getCreatedAt() // 제보일을 기준으로 날짜를 표시
        );
    }
}
