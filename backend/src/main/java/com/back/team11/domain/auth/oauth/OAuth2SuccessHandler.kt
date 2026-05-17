package com.back.team11.domain.auth.oauth

import com.back.team11.domain.auth.service.TokenService
import com.back.team11.global.security.JwtTokenProvider
import com.back.team11.global.util.CookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

private const val REDIRECT_URL = "http://localhost:3000"

@Component
class OAuth2SuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenService: TokenService,
    private val cookieUtil: CookieUtil
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        request.getSession(false)?.invalidate()

        val oAuth2User = authentication.principal as? OAuth2User
            ?: throw IllegalStateException("OAuth2 사용자 정보를 찾을 수 없습니다.")

        val memberId = extractMemberId(oAuth2User)
        val role = oAuth2User.attributes["role"]?.toString()
            ?: throw IllegalStateException("OAuth2 사용자 role 정보를 찾을 수 없습니다.")

        jwtTokenProvider.generateRefreshToken(memberId).also { refreshToken ->
            tokenService.saveOrUpdateRefreshToken(memberId, refreshToken)
            cookieUtil.addRefreshTokenCookie(response, refreshToken)
        }

        cookieUtil.addAccessTokenCookie(response, jwtTokenProvider.generateAccessToken(memberId, role))

        redirectStrategy.sendRedirect(request, response, REDIRECT_URL)
    }

    private fun extractMemberId(oAuth2User: OAuth2User): Long {
        val memberIdValue = oAuth2User.attributes["memberId"]
            ?: throw IllegalStateException("OAuth2 사용자 memberId 정보를 찾을 수 없습니다.")

        return when (memberIdValue) {
            is Long -> memberIdValue
            is Number -> memberIdValue.toLong()  // Int 포함 — Int는 Number 하위타입
            is String -> memberIdValue.toLongOrNull()
                ?: throw IllegalStateException("memberId를 Long 타입으로 변환할 수 없습니다.")
            else -> throw IllegalStateException("지원하지 않는 memberId 타입입니다: ${memberIdValue::class.simpleName}")
        }
    }
}