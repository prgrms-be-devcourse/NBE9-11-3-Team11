package com.back.team11.domain.auth.service

import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.security.JwtTokenProvider
import com.back.team11.global.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class TokenReissueService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenService: TokenService,
    private val cookieUtil: CookieUtil,
    private val memberRepository: MemberRepository
) {


    fun reissue(request: HttpServletRequest, response: HttpServletResponse) {
        val refreshTokenValue = cookieUtil.getRefreshTokenFromCookie(request)
            ?: throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)

        val memberId = jwtTokenProvider.getMemberId(refreshTokenValue)

        // Redis에서 유효성 검증
        if (!tokenService.isValidRefreshToken(memberId, refreshTokenValue)) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }


        // Optional.orElseThrow() + Supplier → ?: throw
        val member = memberRepository.findMemberById(memberId)
            ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        val newAccessToken = jwtTokenProvider.generateAccessToken(memberId, member.role.name)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId)

        tokenService.saveRefreshToken(memberId, newRefreshToken) // Redis 갱신


        cookieUtil.addAccessTokenCookie(response, newAccessToken)
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken)
    }
}