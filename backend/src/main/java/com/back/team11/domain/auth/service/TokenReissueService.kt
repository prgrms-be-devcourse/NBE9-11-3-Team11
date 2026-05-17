package com.back.team11.domain.auth.service

import com.back.team11.domain.auth.repository.RefreshTokenRepository
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.security.JwtTokenProvider
import com.back.team11.global.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TokenReissueService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val cookieUtil: CookieUtil,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun reissue(request: HttpServletRequest, response: HttpServletResponse) {
        val refreshTokenValue = cookieUtil.getRefreshTokenFromCookie(request)
            ?: throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)

        // Optional.orElseThrow() + Supplier → ?: throw
        val refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            ?: throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)

        if (refreshToken.isExpired) {
            refreshTokenRepository.delete(refreshToken)
            throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        }

        // Optional.orElseThrow() + Supplier → ?: throw
        val member = memberRepository.findMemberById(refreshToken.memberId)
            ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        val newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.memberId, member.role.name)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(refreshToken.memberId)
        val newExpiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.refreshTokenExpiration / 1000)

        refreshToken.rotate(newRefreshToken, newExpiresAt)

        cookieUtil.addAccessTokenCookie(response, newAccessToken)
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken)
    }
}