package com.back.team11.domain.auth.controller;

import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.MemberRole;
import com.back.team11.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TokenReissueControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (memberRepository.findByEmail("admin@test.com").isEmpty()) {
            Member admin = new Member();
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setNickname("관리자");
            admin.setRole(MemberRole.ADMIN);
            memberRepository.save(admin);
        }
        memberRepository.flush();
    }


    // 헬퍼: 관리자 로그인 후 쿠키 반환
    private MvcResult loginAsAdmin() throws Exception {
        String requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                """;
        return mvc.perform(
                post("/api/V1/admin/auth/login")
                        .contentType("application/json")
                        .content(requestBody)
        ).andReturn();
    }


    @Test
    @DisplayName("토큰 재발급 성공")
    void t1() throws Exception {

        MvcResult loginResult = loginAsAdmin();
        Cookie accessToken = loginResult.getResponse().getCookie("accessToken");
        Cookie refreshToken = loginResult.getResponse().getCookie("refreshToken");

        // /api/V1/auth/refresh 는 authenticated() → accessToken으로 인증 필요
        ResultActions resultActions = mvc.perform(
                        post("/api/V1/auth/refresh")
                                .cookie(accessToken, refreshToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("토큰 재발급 성공"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없음 (Security에서 401-1 반환)")
    void t2() throws Exception {

        // accessToken 없으면 Security에서 먼저 막음 → 401-1
        ResultActions resultActions = mvc.perform(
                        post("/api/V1/auth/refresh")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 잘못된 refreshToken (accessToken은 유효)")
    void t3() throws Exception {

        MvcResult loginResult = loginAsAdmin();
        Cookie accessToken = loginResult.getResponse().getCookie("accessToken");

        // accessToken으로 인증 통과 후 서비스에서 INVALID_REFRESH_TOKEN → 401-4
        Cookie invalidRefreshToken = new Cookie("refreshToken", "invalid-token");

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/auth/refresh")
                                .cookie(accessToken, invalidRefreshToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-5"));
    }
}