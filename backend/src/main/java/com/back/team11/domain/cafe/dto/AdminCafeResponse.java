package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.cafe.entity.CafeStatus;
import java.time.LocalDateTime;

// 관리자용 응답 DTO — 사용자용 CafeResponse와 달리 status, createdAt 포함
public record AdminCafeResponse(
        CafeBaseInfo cafe,
        CafeStatus status,      // 관리자만 확인 필요
        LocalDateTime createdAt // 관리자만 확인 필요
) {
    // Cafe 엔티티로부터 AdminCafeResponse 생성
    public static AdminCafeResponse from(Cafe cafe) {
        return new AdminCafeResponse(
                CafeBaseInfo.from(cafe),
                cafe.getStatus(),
                cafe.getCreatedAt()
        );
    }
}
