package com.back.team11.domain.auth.dto


data class LoginRequestDto(
    val email: String,
    val password: String
)