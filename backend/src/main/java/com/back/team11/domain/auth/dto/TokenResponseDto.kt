package com.back.team11.domain.auth.dto


data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)