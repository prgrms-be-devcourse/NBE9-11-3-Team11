package com.back.team11.domain.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewRequestDto(
        @NotBlank(message = "리뷰을 작성해주세요")
        String content
) {

}
