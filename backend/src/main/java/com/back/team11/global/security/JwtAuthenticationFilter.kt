package com.back.team11.global.security

import com.back.team11.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val objectMapper = ObjectMapper() // 빈 주입 대신 직접 생성, 별도 빈 등록 불필요

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Authorization 헤더에서 "Bearer {토큰}" 추출
        val token = resolveToken(request)

        // 토큰이 존재하는 경우에만 검증 진행
        // 없으면 다음 필터로 넘김 → SecurityConfig에서 permitAll 여부 판단
        if (StringUtils.hasText(token)) {
            try {
                // 토큰에서 사용자 정보 추출
                val memberId = jwtTokenProvider.getMemberId(token)
                val role = jwtTokenProvider.getRole(token)

                // Spring Security 인증 객체 생성
                // principal 자리에 memberId 저장
                // → 컨트롤러에서 @AuthenticationPrincipal Long memberId 로 꺼낼 수 있음
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_$role"))
                )

                // SecurityContext에 인증 정보 저장
                // 이 줄 있어야 .authenticated() 경로 통과 가능
                SecurityContextHolder.getContext().authentication = authentication

            } catch (e: ExpiredJwtException) {
                // AccessToken 만료 시 진입
                // refresh API 요청은 토큰 재발급을 위해 예외적으로 허용
                // 그 외 요청은 인증 실패 처리
                if (request.requestURI == "/api/V1/auth/refresh") {
                    filterChain.doFilter(request, response)
                    return
                }
                // refresh API가 아닌 경우 → 만료된 토큰으로 접근 불가
                sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN)
                return

            } catch (e: SignatureException) {
                // 서명 불일치 or 토큰 형식 오류
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN)
                return

            } catch (e: MalformedJwtException) {
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN)
                return

            } catch (e: UnsupportedJwtException) {
                // 지원하지 않는 JWT 형식
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN)
                return

            } catch (e: Exception) {
                // 그 외 모든 예외
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN)
                return
            }
        }

        // 토큰 없거나 검증 성공 → 다음 필터로 전달
        filterChain.doFilter(request, response)
    }

    /**
     * 1. 쿠키에서 AccessToken 추출
     * 2. 쿠키 없거나 accessToken 쿠키 없으면 null 반환
     */
    private fun resolveToken(request: HttpServletRequest): String? {
        val cookies = request.cookies ?: return null

        return cookies
            .filter { it.name == "accessToken" }
            .firstOrNull()
            ?.value
    }

    /**
     * 1. 인증 실패 시 JSON 에러 응답
     * 2. 필터에서는 @ExceptionHandler가 동작 안 해서 직접 response에 작성해야 함
     */
    @Throws(IOException::class)
    private fun sendErrorResponse(
        response: HttpServletResponse,
        errorCode: ErrorCode
    ) {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.status = errorCode.httpStatus.value()

        response.writer.write(
            objectMapper.writeValueAsString(
                mapOf(
                    "resultCode" to errorCode.code,
                    "msg" to errorCode.message
                )
            )
        )
    }
}