package com.back.team11.domain.auth.oauth;

import com.back.team11.domain.auth.service.TokenService;
import com.back.team11.global.util.CookieUtil;
import com.back.team11.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

// OAuth2 로그인 성공 직후 JWT를 발급
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        request.getSession().invalidate();

        // 로그인 성공한 사용자 정보 가져오기
        // CustomOAuth2UserService에서 customAttributes에 담아둔 memberId, role 꺼냄
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long memberId = (Long) oAuth2User.getAttributes().get("memberId");
        String role = (String) oAuth2User.getAttributes().get("role");

        // Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(memberId, role);

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);

        // Refresh Token DB 저장
        // 기존에 있으면 rotate(), 없으면 새로 저장
        tokenService.saveOrUpdateRefreshToken(memberId, refreshToken);

        // 쿠키에 토큰 담기 (CookieUtil 사용)
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        //http://localhost:3000/login-success?status=success
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000")
                .build().toUriString();


        // Spring Security 리다이렉트 전략 사용
        // HTTP/HTTPS 자동 처리
        // 상대경로/절대경로 자동 처리
        // 더 안전한 방식
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}