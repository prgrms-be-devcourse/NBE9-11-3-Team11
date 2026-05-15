package com.back.team11.domain.auth.service

import com.back.team11.domain.auth.repository.RefreshTokenRepository
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.security.JwtTokenProvider
import com.back.team11.global.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.function.Supplier

// Access Token 재발급과 예외 처리
@Service
@RequiredArgsConstructor
class TokenReissueService (
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val cookieUtil: CookieUtil,
    private val memberRepository: MemberRepository //추가
){



    @Transactional
    fun reissue(request: HttpServletRequest, response: HttpServletResponse) {
        // Refresh Token은 HttpOnly 쿠키에서만 읽음
        // 쿠키에 없으면 재발급 요청 자체를 유효하지 않은 것으로 보기


        val refreshTokenValue = cookieUtil!!.getRefreshTokenFromCookie(request)

        if (refreshTokenValue == null) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // DB에서 RefreshToken 조회
        // 없으면 위조된 토큰으로 판단 → 예외 처리
        val refreshToken = refreshTokenRepository!!
            .findByToken(refreshTokenValue)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.INVALID_REFRESH_TOKEN) })

        // 만료 여부 확인
        if (refreshToken.isExpired) {
            // 만료된 토큰은 DB에서 삭제
            refreshTokenRepository.delete(refreshToken)
            throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        }


        // 재발급 대상 사용자 식별
        val memberId = refreshToken.memberId

        //수정: MemberRepository에서 실제 role 조회
        val member = memberRepository!!.findById(memberId)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.MEMBER_NOT_FOUND) })
        val role = member.role.name


        val newAccessToken = jwtTokenProvider!!.generateAccessToken(memberId, role)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId)

        // DB에 새 RefreshToken 갱신 (rotate)
        val newExpiresAt = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.refreshTokenExpiration / 1000)
        refreshToken.rotate(newRefreshToken, newExpiresAt)

        // 새 토큰 쿠키에 담기 (CookieUtil 사용)
        cookieUtil.addAccessTokenCookie(response, newAccessToken)
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken)
    }
}