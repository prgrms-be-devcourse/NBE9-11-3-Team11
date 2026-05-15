package com.back.team11.domain.auth.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class TokenResponseDto(
    private val accessToken: String,
    private val refreshToken: String
)