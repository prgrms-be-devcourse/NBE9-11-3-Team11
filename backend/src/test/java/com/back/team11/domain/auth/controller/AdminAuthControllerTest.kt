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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
internal class AdminAuthControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val memberRepository: MemberRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @BeforeEach
    fun setUp() {
        // 관리자 계정 생성
        if (memberRepository!!.findByEmail("admin@test.com").isEmpty()) {
            val admin = Member(
                email = "admin@test.com",
                password = passwordEncoder!!.encode("1234"),
                nickname = "관리자",
                role = MemberRole.ADMIN
            )
            memberRepository.save(admin)
        }

        // 일반 유저 계정 생성
        if (memberRepository.findByEmail("user1@test.com").isEmpty()) {
            val user = Member(
                email = "user1@test.com",
                password = passwordEncoder!!.encode("1234"),
                nickname = "유저1",
                role = MemberRole.USER
            )
            memberRepository.save(user)
        }

        // JWT 필터가 DB를 조회할 수 있도록 flush
        memberRepository.flush()
    }


    @Test
    @DisplayName("관리자 로그인 성공")
    @Throws(Exception::class)
    fun t1() {
        val requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        //로그인 성공 → 200
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("관리자 로그인이 성공적으로 되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))

        //JWT 토큰이 쿠키로 내려오는지 확인
        resultActions
            .andExpect(MockMvcResultMatchers.cookie().exists("accessToken"))
            .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"))
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    @Throws(Exception::class)
    fun t2() {
        val requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "wrong"
                }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        //passwordEncoder.matches 실패 → INVALID_LOGIN
        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-4"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("이메일 또는 비밀번호가 올바르지 않습니다."))
    }

    @Test
    @DisplayName("로그인 실패 - 관리자 아님")
    @Throws(Exception::class)
    fun t3() {
        val requestBody = """
                {
                    "email": "user1@test.com",
                    "password": "1234"
                }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        // 비밀번호는 맞음 → 로그인 통과
        // 하지만 ADMIN 아님 → FORBIDDEN
        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    @Throws(Exception::class)
    fun t4() {
        val requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                
                """.trimIndent()

        // 로그인으로 accessToken + refreshToken 획득
        val loginResult = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        )
            .andReturn()

        val accessToken = loginResult.getResponse().getCookie("accessToken")
        val refreshToken = loginResult.getResponse().getCookie("refreshToken")

        // refresh 엔드포인트는 hasRole("ADMIN") 적용 → accessToken으로 인증 + refreshToken으로 재발급
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/refresh")
                .cookie(accessToken!!, refreshToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("토큰 재발급이 성공적으로 되었습니다."))

        // 새 토큰 다시 내려오는지 확인
        resultActions
            .andExpect(MockMvcResultMatchers.cookie().exists("accessToken"))
            .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없음 (인증 자체가 안 됨 → 401-1)")
    @Throws(Exception::class)
    fun t5() {
        // refresh 엔드포인트는 hasRole("ADMIN") → accessToken 없으면 Security에서 401-1 반환

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/refresh")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 잘못된 refreshToken (accessToken은 있지만 refreshToken이 DB에 없음)")
    @Throws(Exception::class)
    fun t6() {
        val requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                
                """.trimIndent()

        // 로그인으로 유효한 accessToken 획득
        val loginResult = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        )
            .andReturn()

        val accessToken = loginResult.getResponse().getCookie("accessToken")
        // refreshToken은 잘못된 값으로 설정
        val invalidRefreshToken = Cookie("refreshToken", "invalid-token")

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/refresh")
                .cookie(accessToken!!, invalidRefreshToken)
        )
            .andDo(MockMvcResultHandlers.print())

        // 서비스까지 도달하여 INVALID_REFRESH_TOKEN 예외 → 401-4
        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-5"))
    }

    @Test
    @DisplayName("로그아웃 성공")
    @Throws(Exception::class)
    fun t7() {
        val requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                
                """.trimIndent()

        val loginResult = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        )
            .andReturn()

        val accessToken = loginResult.getResponse().getCookie("accessToken")
        val refreshToken = loginResult.getResponse().getCookie("refreshToken")

        // logout도 hasRole("ADMIN") → accessToken으로 인증 필요
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/logout")
                .cookie(accessToken!!, refreshToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그아웃이 성공적으로 되었습니다."))

        // 쿠키 삭제 확인
        resultActions
            .andExpect(MockMvcResultMatchers.cookie().maxAge("accessToken", 0))
            .andExpect(MockMvcResultMatchers.cookie().maxAge("refreshToken", 0))
    }

    @Test
    @DisplayName("로그아웃 실패 - 토큰 없으면 Security에서 401 반환")
    @Throws(Exception::class)
    fun t8() {
        // logout은 hasRole("ADMIN") → accessToken 없으면 인증 실패

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/logout")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }
}