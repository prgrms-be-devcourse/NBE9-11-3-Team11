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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
internal class TokenReissueControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val memberRepository: MemberRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @BeforeEach
    fun setUp() {
        if (memberRepository!!.findByEmail("admin@test.com").isEmpty()) {
            val admin = Member(
                email = "admin@test.com",
                password = passwordEncoder!!.encode("1234"),
                nickname = "관리자",
                role = MemberRole.ADMIN
            )
            memberRepository.save(admin)
        }
        memberRepository.flush()
    }


    // 헬퍼: 관리자 로그인 후 쿠키 반환
    @Throws(Exception::class)
    private fun loginAsAdmin(): MvcResult {
        val requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                
                """.trimIndent()
        return mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        ).andReturn()
    }


    @Test
    @DisplayName("토큰 재발급 성공")
    @Throws(Exception::class)
    fun t1() {
        val loginResult = loginAsAdmin()
        val accessToken = loginResult.getResponse().getCookie("accessToken")
        val refreshToken = loginResult.getResponse().getCookie("refreshToken")

        // /api/V1/auth/refresh 는 authenticated() → accessToken으로 인증 필요
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/auth/refresh")
                .cookie(accessToken!!, refreshToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("토큰 재발급 성공"))
            .andExpect(MockMvcResultMatchers.cookie().exists("accessToken"))
            .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없음 (Security에서 401-1 반환)")
    @Throws(Exception::class)
    fun t2() {
        // accessToken 없으면 Security에서 먼저 막음 → 401-1

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/auth/refresh")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 잘못된 refreshToken (accessToken은 유효)")
    @Throws(Exception::class)
    fun t3() {
        val loginResult = loginAsAdmin()
        val accessToken = loginResult.getResponse().getCookie("accessToken")

        // accessToken으로 인증 통과 후 서비스에서 INVALID_REFRESH_TOKEN → 401-4
        val invalidRefreshToken = Cookie("refreshToken", "invalid-token")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/auth/refresh")
                .cookie(accessToken!!, invalidRefreshToken)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-5"))
    }
}