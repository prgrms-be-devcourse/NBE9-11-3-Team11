package com.back.team11.global.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function

//쿠키 생성/삭제/조회 유틸 클래스
@Component
class CookieUtil {
    //Access Token 쿠키 추가
    //HttpOnly → JS에서 접근 불가 (보안 강화)
    //SameSite=Strict → 같은 사이트 요청에서만 쿠키 전송
    fun addAccessTokenCookie(response: HttpServletResponse, value: String?) {
        val cookie = Cookie("accessToken", value)
        cookie.setPath("/")
        cookie.setHttpOnly(true)
        cookie.setMaxAge(ACCESS_TOKEN_MAX_AGE)
        //cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie)
    }

    // Refresh Token 쿠키 추가
    fun addRefreshTokenCookie(response: HttpServletResponse, value: String?) {
        val cookie = Cookie("refreshToken", value)
        cookie.setPath("/")
        cookie.setHttpOnly(true)
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE)
        //cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie)
    }


    //Access Token 쿠키 삭제
    // MaxAge = 0 → 즉시 만료
    // 존 쿠키를 빈 값으로 덮어써서 삭제(로그아웃 시 사용)
    fun deleteAccessTokenCookie(response: HttpServletResponse) {
        val cookie = Cookie("accessToken", "")
        cookie.setPath("/")
        cookie.setHttpOnly(true)
        cookie.setMaxAge(0)
        response.addCookie(cookie)
    }

    //Refresh Token 쿠키 삭제
    fun deleteRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = Cookie("refreshToken", "")
        cookie.setPath("/")
        cookie.setHttpOnly(true)
        cookie.setMaxAge(0)
        response.addCookie(cookie)
    }


    // 요청 쿠키에서 refreshToken 값 조회
    // 쿠키 배열이 없거나 refreshToken이 없으면 null 반환
    // Service 레이어에서 null 체크 후 예외 처리 필요!
    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? {
        val cookies = request.getCookies()

        if (cookies == null) {
            return null
        }

        return Arrays.stream<Cookie?>(cookies)
            .filter { cookie: Cookie? -> "refreshToken" == cookie!!.getName() }
            .findFirst()
            .map<String?>(Function { obj: Cookie? -> obj!!.getValue() })
            .orElse(null)
    }

    companion object {
        // Access Token 만료 시간: 30분 (초 단위)
        private const val ACCESS_TOKEN_MAX_AGE = 1800

        // Refresh Token 만료 시간: 7일 (초 단위)
        private const val REFRESH_TOKEN_MAX_AGE = 604800
    }
}