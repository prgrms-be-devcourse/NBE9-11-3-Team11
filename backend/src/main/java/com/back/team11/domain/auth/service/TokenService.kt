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
    fun saveOrUpdateRefreshToken(
        memberId: Long?,
        refreshToken: String
    ) {
        val validMemberId = memberId
            ?: throw IllegalArgumentException("memberId는 null일 수 없습니다.")

        val expiresAt = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.refreshTokenExpiration / 1000)

        val token = refreshTokenRepository.findByMemberId(validMemberId)
            .map { existingToken ->
                existingToken.rotate(refreshToken, expiresAt)
                existingToken
            }
            .orElseGet {
                RefreshToken(
                    memberId = validMemberId,
                    token = refreshToken,
                    expiresAt = expiresAt
                )
            }

        refreshTokenRepository.save(token)
    }
}