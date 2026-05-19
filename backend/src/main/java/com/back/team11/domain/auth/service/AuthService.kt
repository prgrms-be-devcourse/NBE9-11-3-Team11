package com.back.team11.domain.auth.service

import com.back.team11.domain.auth.dto.LoginRequestDto
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

@Service
@Transactional(readOnly = true)
class AuthService(
    private val cookieUtil: CookieUtil,
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenService: TokenService
) {

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
        val refreshToken = jwtTokenProvider.generateRefreshToken(memberId)

        tokenService.saveRefreshToken(memberId, refreshToken) // Redis에 저장

        cookieUtil.addAccessTokenCookie(response, accessToken)
        cookieUtil.addRefreshTokenCookie(response, refreshToken)
    }

    fun logout(request: HttpServletRequest, response: HttpServletResponse) {

        val accessToken = cookieUtil.getAccessTokenFromCookie(request)
        val refreshToken = cookieUtil.getRefreshTokenFromCookie(request)

        // AccessToken 블랙리스트 등록
        if (accessToken != null) {
            val remaining = jwtTokenProvider.getRemainingExpiry(accessToken)
            tokenService.addBlacklist(accessToken, remaining)
        }

        // RefreshToken 삭제
        if (refreshToken != null) {
            val memberId = jwtTokenProvider.getMemberId(refreshToken)
            tokenService.deleteRefreshToken(memberId)
        }

        cookieUtil.deleteAccessTokenCookie(response)
        cookieUtil.deleteRefreshTokenCookie(response)
    }
}