package com.back.team11.domain.review.dto

import com.back.team11.domain.review.entity.Review
import java.time.LocalDateTime

data class ReviewResponseDto(
    val id: Long,
    val cafeId: Long,
    val memberId: Long,
    val nickname: String,
    val content: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(review: Review): ReviewResponseDto =
            ReviewResponseDto(
                review.id,
                review.cafe.id,
                review.member.id,
                review.member.nickname,
                review.content,
                review.createdAt!!
            )
    }
}
