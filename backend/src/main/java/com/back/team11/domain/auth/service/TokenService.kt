package com.back.team11.domain.auth.service

import com.back.team11.domain.auth.entity.RefreshToken
import com.back.team11.domain.auth.repository.RefreshTokenRepository
import com.back.team11.global.security.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun saveOrUpdateRefreshToken(memberId: Long, refreshToken: String) {
        // nullable → non-null로 시그니처 자체를 수정
        val expiresAt = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.refreshTokenExpiration / 1000)

        // Optional.map().orElseGet() → ?.also { } ?: RefreshToken(...)
        val token = refreshTokenRepository.findByMemberId(memberId)
            ?.also { it.rotate(refreshToken, expiresAt) }
            ?: RefreshToken(memberId = memberId, token = refreshToken, expiresAt = expiresAt)

        refreshTokenRepository.save(token)
    }
}