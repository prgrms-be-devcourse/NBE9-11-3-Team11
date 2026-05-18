package com.back.team11.global.security

import com.back.team11.domain.auth.oauth.CustomOAuth2UserService
import com.back.team11.domain.auth.oauth.OAuth2SuccessHandler
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val customOAuth2UserService: CustomOAuth2UserService,
) {
    /**
     * HTTP 요청에 대한 보안 필터 체인 설정
     * 인증/인가 규칙, 세션 정책, CSRF, 예외 처리 등을 정의
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) } // CORS 설정 적용
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // JWT 기반이므로 세션 미사용
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // Swagger - 인증 없이 접근 허용
                    .requestMatchers(HttpMethod.GET, "/api/V1/auth/me").authenticated() // 내 정보 조회 - 인증 필요
                    .requestMatchers(HttpMethod.POST, "/api/V1/auth/logout").authenticated() // 로그아웃 - 인증 필요
                    .requestMatchers(HttpMethod.GET, "/api/V1/cafe").permitAll() // 카페 목록 조회 - 비로그인 허용
                    .requestMatchers(HttpMethod.GET, "/api/V1/cafe/*").permitAll() // 카페 상세 조회 - 비로그인 허용
                    .requestMatchers(HttpMethod.GET, "/api/V1/cafe/*/reviews").permitAll() // 리뷰 조회 - 비로그인 허용
                    .requestMatchers(HttpMethod.GET, "/api/V1/cafe/*/reviews/page").permitAll() // 리뷰 페이지 조회 - 비로그인 허용
                    .requestMatchers("/", "/login", "/error", "/api/V1/auth/oauth/**", "/login/oauth2/**").permitAll() // OAuth 인증 흐름 - 인증 전 접근 허용
                    .requestMatchers("/api/V1/admin/auth/login").permitAll() // 관리자 로그인 - 인증 전 접근 허용
                    .requestMatchers("/api/V1/admin/**").hasRole("ADMIN") // 관리자 전용 API - ADMIN 권한 필요
                    .requestMatchers("/api/*/**").authenticated() // 나머지 API - 로그인 필요
                    .anyRequest().authenticated()
            }
            .csrf { it.disable() } // REST API이므로 CSRF 비활성화
            .formLogin { it.disable() } // 폼 로그인 비활성화
            .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
            .oauth2Login { oauth2 ->
                with(oauth2) {
                    authorizationEndpoint { it.baseUri("/api/V1/auth/oauth") } // OAuth 시작 엔드포인트
                    redirectionEndpoint { it.baseUri("/api/V1/auth/oauth/*/callback") } // OAuth 콜백 URL
                    defaultSuccessUrl("/loginSuccess", true)
                    failureUrl("/login?error")
                    userInfoEndpoint { it.userService(customOAuth2UserService) } // 사용자 정보 처리 서비스
                    successHandler(oAuth2SuccessHandler) // 로그인 성공 시 JWT 발급
                    failureHandler { _, response, _ -> // 로그인 실패 시 401 응답
                        response.sendJsonError(401, "401-1", "소셜 로그인에 실패했습니다.")
                    }
                }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java) // JWT 필터를 기본 로그인 필터 앞에 실행
            .exceptionHandling { exception ->
                exception.authenticationEntryPoint { _, response, _ -> // 인증 실패 (토큰 없음/만료) → 401
                    response.sendJsonError(401, "401-1", "로그인 후 이용해주세요.")
                }
                exception.accessDeniedHandler { _, response, _ -> // 권한 부족 → 403
                    response.sendJsonError(403, "403-1", "접근 권한이 없습니다.")
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000") // 현재 로컬 개발 환경만 허용
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드 목록
            allowedHeaders = listOf("*") // 모든 요청 헤더 허용
            allowCredentials = true // 쿠키/인증 정보 포함 요청 허용
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", configuration) // /api/** 경로에 위 CORS 설정 적용
        }
    }

    // BCrypt 암호화 방식으로 비밀번호 인코딩, 구현체 사용
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}

// 필터/핸들러에서 @ExceptionHandler가 동작 안 해서 직접 response에 작성해야 함
private fun HttpServletResponse.sendJsonError(status: Int, resultCode: String, msg: String) {
    contentType = "application/json"
    characterEncoding = "UTF-8"
    this.status = status
    writer.write("""{"resultCode": "$resultCode", "msg": "$msg"}""")
}