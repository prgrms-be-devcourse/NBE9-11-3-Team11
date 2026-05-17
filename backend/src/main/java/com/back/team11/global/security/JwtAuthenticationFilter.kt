package com.back.team11.global.security

import com.back.team11.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
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
        // 쿠키에서 accessToken 추출, 없으면 다음 필터로 넘김 → SecurityConfig에서 permitAll 여부 판단
        resolveToken(request)?.let { token ->
            try {
                val memberId = jwtTokenProvider.getMemberId(token)
                val role = jwtTokenProvider.getRole(token)

                // principal 자리에 memberId 저장
                // → 컨트롤러에서 @AuthenticationPrincipal Long memberId 로 꺼낼 수 있음
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(
                        memberId,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_$role"))
                    )

            } catch (e: ExpiredJwtException) {
                // refresh API 요청은 토큰 재발급을 위해 예외적으로 허용
                if (request.requestURI == "/api/V1/auth/refresh") {
                    filterChain.doFilter(request, response)
                    return
                }
                sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN)
                return

            } catch (e: Exception) {
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? =
        request.cookies
            ?.firstOrNull { it.name == "accessToken" }
            ?.value

    // 필터에서는 @ExceptionHandler가 동작 안 해서 직접 response에 작성해야 함
    @Throws(IOException::class)
    private fun sendErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.status = errorCode.httpStatus.value()
        response.writer.write(
            objectMapper.writeValueAsString(
                mapOf("resultCode" to errorCode.code, "msg" to errorCode.message)
            )
        )
    }
}