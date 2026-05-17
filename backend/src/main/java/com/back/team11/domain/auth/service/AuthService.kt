package com.back.team11.domain.auth.service

import com.back.team11.domain.auth.dto.LoginRequestDto
import com.back.team11.domain.auth.entity.RefreshToken
import com.back.team11.domain.auth.repository.RefreshTokenRepository
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.service.MemberService
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
@Transactional(readOnly = true)
class AuthService(
    private val cookieUtil: CookieUtil,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun adminLogin(loginRequestDto: LoginRequestDto, response: HttpServletResponse) {
        val member = memberService.findByEmail(loginRequestDto.email)
            ?: throw CustomException(ErrorCode.INVALID_LOGIN)

        if (!memberService.validatePassword(loginRequestDto.password, member.password ?: throw CustomException(ErrorCode.INVALID_LOGIN))) {
            throw CustomException(ErrorCode.INVALID_LOGIN)
        }

        if (member.role != MemberRole.ADMIN) {
            throw CustomException(ErrorCode.FORBIDDEN)
        }

        // member.id 중복 제거 — 한 번만 검증
        val memberId = member.id ?: throw IllegalStateException("Member ID가 없습니다.")

        val accessToken = jwtTokenProvider.generateAccessToken(memberId, member.role.name)
        val refreshTokenValue = jwtTokenProvider.generateRefreshToken(memberId)
        val expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.refreshTokenExpiration / 1000)

        // Optional.map().orElseGet() → 코틀린 let + elvis
        val refreshToken = refreshTokenRepository.findByMemberId(memberId)
            ?.also { it.rotate(refreshTokenValue, expiresAt) }
            ?: RefreshToken(memberId = memberId, token = refreshTokenValue, expiresAt = expiresAt)

        refreshTokenRepository.save(refreshToken)

        cookieUtil.addAccessTokenCookie(response, accessToken)
        cookieUtil.addRefreshTokenCookie(response, refreshTokenValue)
    }

    @Transactional
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        cookieUtil.getRefreshTokenFromCookie(request)
            ?.let { refreshTokenRepository.findByToken(it) }
            ?.let { refreshTokenRepository.delete(it) }

        cookieUtil.deleteAccessTokenCookie(response)
        cookieUtil.deleteRefreshTokenCookie(response)
    }
}