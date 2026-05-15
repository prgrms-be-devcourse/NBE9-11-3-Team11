package com.back.team11.domain.review.dto

import jakarta.validation.constraints.NotBlank

data class ReviewRequestDto(
    @field: NotBlank(message = "리뷰을 작성해주세요")
    val content: String
)
