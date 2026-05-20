package com.back.team11.global.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

//쿠키 생성/삭제/조회 유틸 클래스
@Component
class CookieUtil {

    companion object {
        // Access Token 만료 시간: 30분 (초 단위)
        private const val ACCESS_TOKEN_MAX_AGE = 1800

        // Refresh Token 만료 시간: 7일 (초 단위)
        private const val REFRESH_TOKEN_MAX_AGE = 604800
    }

    private fun buildCookie(name: String, value: String?, maxAge: Int) =
        Cookie(name, value).apply {
            path = "/"
            isHttpOnly = true
            this.maxAge = maxAge
        }

    //Access Token 쿠키 추가
    //HttpOnly → JS에서 접근 불가 (보안 강화)
    //SameSite=Strict → 같은 사이트 요청에서만 쿠키 전송
    fun addAccessTokenCookie(response: HttpServletResponse, value: String?) =
        response.addCookie(buildCookie("accessToken", value, ACCESS_TOKEN_MAX_AGE))

    // Refresh Token 쿠키 추가
    fun addRefreshTokenCookie(response: HttpServletResponse, value: String?) =
        response.addCookie(buildCookie("refreshToken", value, REFRESH_TOKEN_MAX_AGE))


    //Access Token 쿠키 삭제
    // MaxAge = 0 → 즉시 만료
    // 존 쿠키를 빈 값으로 덮어써서 삭제(로그아웃 시 사용)
    fun deleteAccessTokenCookie(response: HttpServletResponse) =
        response.addCookie(buildCookie("accessToken", "", maxAge = 0))

    //Refresh Token 쿠키 삭제
    fun deleteRefreshTokenCookie(response: HttpServletResponse) =
        response.addCookie(buildCookie("refreshToken", "", maxAge = 0))


    // 요청 쿠키에서 refreshToken 값 조회
    // 쿠키 배열이 없거나 refreshToken이 없으면 null 반환
    // Service 레이어에서 null 체크 후 예외 처리 필요!
    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? =
        request.cookies
            ?.firstOrNull { it.name == "refreshToken" }
            ?.value

    // AccessToken 쿠키 조회
    fun getAccessTokenFromCookie(request: HttpServletRequest): String? =
        request.cookies
            ?.firstOrNull { it.name == "accessToken" }
            ?.value
}