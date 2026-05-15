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
    fun adminLogin(
        loginRequestDto: LoginRequestDto,
        response: HttpServletResponse
    ) {
        val member = memberService.findByEmail(loginRequestDto.email)

        if (
            member == null ||
            !memberService.validatePassword(loginRequestDto.password, member.password ?: throw CustomException(ErrorCode.INVALID_LOGIN))
        ) {
            throw CustomException(ErrorCode.INVALID_LOGIN)
        }

        // 관리자 로그인 컨트롤러이므로 ADMIN만 허용
        if (member.role != MemberRole.ADMIN) {
            throw CustomException(ErrorCode.FORBIDDEN)
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            member.id ?: throw IllegalStateException("Member ID가 없습니다."),
            member.role.name
        )

        val refreshTokenValue = jwtTokenProvider.generateRefreshToken(member.id ?: throw IllegalStateException("Member ID가 없습니다."))

        val expiresAt = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.refreshTokenExpiration / 1000)

        val refreshToken = refreshTokenRepository.findByMemberId(member.id?: throw IllegalStateException("Member ID가 없습니다."))
            .map { existingToken ->
                existingToken.rotate(refreshTokenValue, expiresAt)
                existingToken
            }
            .orElseGet {
                RefreshToken(
                    memberId = member.id ?: throw IllegalStateException("Member ID가 없습니다."),
                    token = refreshTokenValue,
                    expiresAt = expiresAt
                )
            }

        refreshTokenRepository.save(refreshToken)

        cookieUtil.addAccessTokenCookie(response, accessToken)
        cookieUtil.addRefreshTokenCookie(response, refreshTokenValue)
    }

    @Transactional
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val refreshToken = cookieUtil.getRefreshTokenFromCookie(request)

        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                .ifPresent { refreshTokenEntity ->
                    refreshTokenRepository.delete(refreshTokenEntity)
                }
        }

        cookieUtil.deleteAccessTokenCookie(response)
        cookieUtil.deleteRefreshTokenCookie(response)
    }
}