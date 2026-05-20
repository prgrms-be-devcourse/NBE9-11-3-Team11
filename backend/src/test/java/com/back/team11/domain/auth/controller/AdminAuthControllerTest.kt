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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
internal class AdminAuthControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        // 관리자 계정 생성
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

        // 일반 유저 계정 생성
        if (memberRepository.findByEmail("user1@test.com") == null) {
            memberRepository.save(
                Member(
                    email = "user1@test.com",
                    password = passwordEncoder.encode("1234"),
                    nickname = "유저1",
                    role = MemberRole.USER
                )
            )
        }

        // JWT 필터가 DB를 조회할 수 있도록 flush
        memberRepository.flush()
    }

    @Test
    @DisplayName("관리자 로그인 성공")
    fun t1() {
        val resultActions = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "admin@test.com", "password": "1234"}""")
        ).andDo(print())

        // 로그인 성공 → 200
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("관리자 로그인이 성공적으로 되었습니다."))
            .andExpect(jsonPath("$.resultCode").value("200"))

        // JWT 토큰이 쿠키로 내려오는지 확인
        resultActions
            .andExpect(cookie().exists("accessToken"))
            .andExpect(cookie().exists("refreshToken"))
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    fun t2() {
        val resultActions = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "admin@test.com", "password": "wrong"}""")
        ).andDo(print())

        // passwordEncoder.matches 실패 → INVALID_LOGIN
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-4"))
            .andExpect(jsonPath("$.msg").value("이메일 또는 비밀번호가 올바르지 않습니다."))
    }

    @Test
    @DisplayName("로그인 실패 - 관리자 아님")
    fun t3() {
        val resultActions = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "user1@test.com", "password": "1234"}""")
        ).andDo(print())

        // 비밀번호는 맞음 → 로그인 통과
        // 하지만 ADMIN 아님 → FORBIDDEN
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    fun t4() {
        // 로그인으로 accessToken + refreshToken 획득
        val loginResult = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "admin@test.com", "password": "1234"}""")
        ).andReturn()

        val accessToken = loginResult.response.getCookie("accessToken")!!
        val refreshToken = loginResult.response.getCookie("refreshToken")!!

        // refresh 엔드포인트는 hasRole("ADMIN") 적용 → accessToken으로 인증 + refreshToken으로 재발급
        val resultActions = mvc.perform(
            post("/api/V1/admin/auth/refresh")
                .cookie(accessToken, refreshToken)
        ).andDo(print())

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("토큰 재발급이 성공적으로 되었습니다."))

        // 새 토큰 다시 내려오는지 확인
        resultActions
            .andExpect(cookie().exists("accessToken"))
            .andExpect(cookie().exists("refreshToken"))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없음 (인증 자체가 안 됨 → 401-1)")
    fun t5() {
        // refresh 엔드포인트는 hasRole("ADMIN") → accessToken 없으면 Security에서 401-1 반환
        mvc.perform(post("/api/V1/admin/auth/refresh"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 잘못된 refreshToken (accessToken은 있지만 refreshToken이 DB에 없음)")
    fun t6() {
        // 로그인으로 유효한 accessToken 획득
        val loginResult = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "admin@test.com", "password": "1234"}""")
        ).andReturn()

        val accessToken = loginResult.response.getCookie("accessToken")!!
        // refreshToken은 잘못된 값으로 설정
        val invalidRefreshToken = Cookie("refreshToken", "invalid-token")

        val resultActions = mvc.perform(
            post("/api/V1/admin/auth/refresh")
                .cookie(accessToken, invalidRefreshToken)
        ).andDo(print())

        // 서비스까지 도달하여 INVALID_REFRESH_TOKEN 예외 → 401-5
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-5"))
    }

    @Test
    @DisplayName("로그아웃 성공")
    fun t7() {
        val loginResult = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content("""{"email": "admin@test.com", "password": "1234"}""")
        ).andReturn()

        val accessToken = loginResult.response.getCookie("accessToken")!!
        val refreshToken = loginResult.response.getCookie("refreshToken")!!

        // logout도 hasRole("ADMIN") → accessToken으로 인증 필요
        val resultActions = mvc.perform(
            post("/api/V1/admin/auth/logout")
                .cookie(accessToken, refreshToken)
        ).andDo(print())

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("로그아웃이 성공적으로 되었습니다."))

        // 쿠키 삭제 확인
        resultActions
            .andExpect(cookie().maxAge("accessToken", 0))
            .andExpect(cookie().maxAge("refreshToken", 0))
    }

    @Test
    @DisplayName("로그아웃 실패 - 토큰 없으면 Security에서 401 반환")
    fun t8() {
        // logout은 hasRole("ADMIN") → accessToken 없으면 인증 실패
        mvc.perform(post("/api/V1/admin/auth/logout"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }
}