package com.back.team11.domain.review.dto;

import com.back.team11.domain.review.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long id,
        Long cafeId,
        Long memberId,
        String nickname,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getCafe().getId(),
                review.getMember().getId(),
                review.getMember().getNickname(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
