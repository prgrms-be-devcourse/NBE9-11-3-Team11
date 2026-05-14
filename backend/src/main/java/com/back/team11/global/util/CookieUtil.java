package com.back.team11.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;

//쿠키 생성/삭제/조회 유틸 클래스
@Component
public class CookieUtil {

    // Access Token 만료 시간: 30분 (초 단위)
    private static final int ACCESS_TOKEN_MAX_AGE = 1800;

    // Refresh Token 만료 시간: 7일 (초 단위)
    private static final int REFRESH_TOKEN_MAX_AGE = 604800;


     //Access Token 쿠키 추가
     //HttpOnly → JS에서 접근 불가 (보안 강화)
     //SameSite=Strict → 같은 사이트 요청에서만 쿠키 전송
    public void addAccessTokenCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie("accessToken", value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(ACCESS_TOKEN_MAX_AGE);
        //cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    // Refresh Token 쿠키 추가
    public void addRefreshTokenCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie("refreshToken", value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        //cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }


    //Access Token 쿠키 삭제
    // MaxAge = 0 → 즉시 만료
    // 존 쿠키를 빈 값으로 덮어써서 삭제(로그아웃 시 사용)
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    //Refresh Token 쿠키 삭제
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


    // 요청 쿠키에서 refreshToken 값 조회
    // 쿠키 배열이 없거나 refreshToken이 없으면 null 반환
    // Service 레이어에서 null 체크 후 예외 처리 필요!
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}