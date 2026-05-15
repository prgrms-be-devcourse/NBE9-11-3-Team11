package com.back.team11.domain.auth.controller

import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
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
internal class AuthControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val memberRepository: MemberRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @BeforeEach
    fun setUp() {
        // 관리자 계정 생성 (admin 로그인 엔드포인트를 통해 accessToken 획득하기 위함)
        if (memberRepository!!.findByEmail("admin@test.com").isEmpty()) {
            val admin = Member(
                email = "admin@test.com",
                password = passwordEncoder!!.encode("1234"),
                nickname = "관리자",
                role = MemberRole.ADMIN
            )
            memberRepository.save(admin)
        }

        //영속성 컨텍스트에만 있으면 Security(JWT 필터)에서 조회 안될 수 있음
        //DB에 실제 반영해서 인증 시 조회 가능하도록 함
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
    @DisplayName("내 정보 조회 성공")
    @Throws(Exception::class)
    fun t1() {
        //로그인 → accessToken 확보

        val loginResult = loginAsAdmin()
        val accessToken = loginResult.getResponse().getCookie("accessToken")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/auth/me")
                .cookie(accessToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("내 정보 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("admin@test.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberId").exists())
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 인증 없음 (401)")
    @Throws(Exception::class)
    fun t2() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/auth/me")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("로그아웃 성공")
    @Throws(Exception::class)
    fun t3() {
        //로그인 → 토큰 확보

        val loginResult = loginAsAdmin()
        val accessToken = loginResult.getResponse().getCookie("accessToken")
        val refreshToken = loginResult.getResponse().getCookie("refreshToken")

        // /api/V1/auth/logout 은 authenticated() → accessToken으로 인증 필요
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/auth/logout")
                .cookie(accessToken!!, refreshToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그아웃 성공")) //로그아웃 시 쿠키 삭제 확인
            .andExpect(MockMvcResultMatchers.cookie().maxAge("accessToken", 0))
            .andExpect(MockMvcResultMatchers.cookie().maxAge("refreshToken", 0))
    }

    @Test
    @DisplayName("로그아웃 실패 - 토큰 없으면 Security에서 401 반환")
    @Throws(Exception::class)
    fun t4() {
        // /api/V1/auth/logout 은 authenticated() → 토큰 없으면 401-1

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/auth/logout")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }
}