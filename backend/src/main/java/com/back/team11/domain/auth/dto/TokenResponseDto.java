package com.back.team11.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;

    public TokenResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}