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
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

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

        val accessToken = jwtTokenProvider.generateAccessToken(memberId, role)

        val refreshToken = jwtTokenProvider.generateRefreshToken(memberId)

        tokenService.saveOrUpdateRefreshToken(memberId, refreshToken)

        cookieUtil.addAccessTokenCookie(response, accessToken)
        cookieUtil.addRefreshTokenCookie(response, refreshToken)

        val targetUrl = UriComponentsBuilder
            .fromUriString("http://localhost:3000")
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun extractMemberId(oAuth2User: OAuth2User): Long {
        val memberIdValue = oAuth2User.attributes["memberId"]
            ?: throw IllegalStateException("OAuth2 사용자 memberId 정보를 찾을 수 없습니다.")

        return when (memberIdValue) {
            is Long -> memberIdValue
            is Int -> memberIdValue.toLong()
            is Number -> memberIdValue.toLong()
            is String -> memberIdValue.toLongOrNull()
                ?: throw IllegalStateException("memberId를 Long 타입으로 변환할 수 없습니다.")

            else -> throw IllegalStateException("지원하지 않는 memberId 타입입니다: ${memberIdValue::class.simpleName}")
        }
    }
}