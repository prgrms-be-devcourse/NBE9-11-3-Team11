package com.back.team11.domain.auth.dto


class TokenResponseDto(
    private val accessToken: String,
    private val refreshToken: String
)