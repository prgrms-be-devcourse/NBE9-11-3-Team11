package com.back.team11.security;

import com.back.team11.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

// 직접 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();// 빈 주입 대신 직접 생성, 별도 빈 등록 불필요

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 "Bearer {토큰}" 추출
        String token = resolveToken(request);

        // 토큰이 존재하는 경우에만 검증 진행
        // 없으면 다음 필터로 넘김 → SecurityConfig에서 permitAll 여부 판단
        if (StringUtils.hasText(token)) {
            try {
                // 토큰에서 사용자 정보 추출
                Long memberId = jwtTokenProvider.getMemberId(token);
                String role = jwtTokenProvider.getRole(token);

                // Spring Security 인증 객체 생성
                // principal 자리에 memberId 저장
                // → 컨트롤러에서 @AuthenticationPrincipal Long memberId 로 꺼낼 수 있음
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // SecurityContext에 인증 정보 저장
                // 이 줄 있어야 .authenticated() 경로 통과 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                // AccessToken 만료 시 진입
                // refresh API 요청은 토큰 재발급을 위해 예외적으로 허용
                // 그 외 요청은 인증 실패 처리
                if (request.getRequestURI().equals("/api/V1/auth/refresh")) {
                    filterChain.doFilter(request, response);
                    return;
                }
                // refresh API가 아닌 경우 → 만료된 토큰으로 접근 불가
                sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN);
                return;

            } catch (SignatureException | MalformedJwtException e) {
                // 서명 불일치 or 토큰 형식 오류
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;

            } catch (UnsupportedJwtException e) {
                // 지원하지 않는 JWT 형식
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;

            } catch (Exception e) {
                // 그 외 모든 예외
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            }
        }

        // 토큰 없거나 검증 성공 → 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     1.쿠키에서 AccessToken 추출
     2. 쿠키 없거나 accessToken 쿠키 없으면 null 반환
     */
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    /**
     1. 인증 실패 시 JSON 에러 응답
     2. 필터에서는 @ExceptionHandler가 동작 안 해서 직접 response에 작성해야 함
     */
    private void sendErrorResponse(HttpServletResponse response,
                                   ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Map.of(
                                "resultCode", errorCode.getCode(),
                                "msg", errorCode.getMessage()
                        )
                )
        );
    }
}