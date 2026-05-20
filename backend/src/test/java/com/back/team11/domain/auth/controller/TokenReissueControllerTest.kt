package com.back.team11.domain.auth.controller

import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
internal class TokenReissueControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        if (memberRepository.findByEmail("admin@test.com") == null) {
            memberRepository.save(
                Member(
                    email = "admin@test.com",
                    password = passwordEncoder.encode("1234"),
                    nickname = "관리자",
                    role = MemberRole.ADMIN
                )
            )
        }
        memberRepository.flush()
    }

    // 헬퍼: 관리자 로그인 후 쿠키 반환
    private fun loginAsAdmin(): MvcResult =
        mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "admin@test.com", "password": "1234"}""")
        ).andReturn()

    @Test
    @DisplayName("토큰 재발급 성공")
    fun t1() {
        val loginResult = loginAsAdmin()
        val accessToken = loginResult.response.getCookie("accessToken")!!
        val refreshToken = loginResult.response.getCookie("refreshToken")!!

        // /api/V1/auth/refresh 는 authenticated() → accessToken으로 인증 필요
        mvc.perform(
            post("/api/V1/auth/refresh").cookie(accessToken, refreshToken)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("토큰 재발급 성공"))
            .andExpect(cookie().exists("accessToken"))
            .andExpect(cookie().exists("refreshToken"))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없음 (Security에서 401-1 반환)")
    fun t2() {
        // accessToken 없으면 Security에서 먼저 막음 → 401-1
        mvc.perform(post("/api/V1/auth/refresh"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 잘못된 refreshToken (accessToken은 유효)")
    fun t3() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")!!

        // accessToken으로 인증 통과 후 서비스에서 INVALID_REFRESH_TOKEN → 401-4
        val invalidRefreshToken = Cookie("refreshToken", "invalid-token")

        mvc.perform(
            post("/api/V1/auth/refresh").cookie(accessToken, invalidRefreshToken)
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-5"))
    }
}