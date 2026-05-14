package com.back.team11.domain.cafe.controller;

import com.back.team11.domain.cafe.entity.CafeType;
import com.back.team11.domain.cafe.entity.CongestionLevel;
import com.back.team11.domain.cafe.entity.FloorCount;
import com.back.team11.domain.cafe.entity.Franchise;
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
import org.springframework.http.MediaType;
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
class CafeControllerTest {

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

    // 헬퍼: 관리자 로그인 후 accessToken 반환
    private Cookie loginAndGetAccessToken() throws Exception {
        String requestBody = """
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
                """;
        MvcResult result = mvc.perform(
                post("/api/V1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andReturn();

        return result.getResponse().getCookie("accessToken");
    }


    @Test
    @DisplayName("카페 제보 성공")
    void t1() throws Exception {

        Cookie accessToken = loginAndGetAccessToken();

        String requestBody = """
                {
                    "name": "스터디 카페",
                    "address": "서울시 강남구 테헤란로 1",
                    "latitude": 37.123456,
                    "longitude": 127.123456,
                    "phone": "02-1234-5678",
                    "description": "조용한 카페입니다.",
                    "type": "%s",
                    "franchise": "%s",
                    "hasToilet": true,
                    "hasOutlet": true,
                    "hasWifi": true,
                    "floorCount": "%s",
                    "hasSeparateSpace": false,
                    "congestionLevel": "%s",
                    "imageUrl": "https://example.com/image.jpg"
                }
                """.formatted(
                CafeType.values()[0].name(),
                Franchise.values()[0].name(),
                FloorCount.values()[0].name(),
                CongestionLevel.values()[0].name()
        );

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/report")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("카페 제보가 접수되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.data.cafe.name").value("스터디 카페"))
                .andExpect(jsonPath("$.data.cafe.address").value("서울시 강남구 테헤란로 1"))
                .andExpect(jsonPath("$.data.cafe.cafeId").exists())
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("카페 제보 실패 - 인증 없음")
    void t2() throws Exception {

        String requestBody = """
                {
                    "name": "스터디 카페",
                    "address": "서울시 강남구 테헤란로 1",
                    "latitude": 37.123456,
                    "longitude": 127.123456,
                    "type": "%s",
                    "franchise": "%s",
                    "hasToilet": true,
                    "hasOutlet": true,
                    "hasWifi": true,
                    "floorCount": "%s",
                    "hasSeparateSpace": false,
                    "congestionLevel": "%s"
                }
                """.formatted(
                CafeType.values()[0].name(),
                Franchise.values()[0].name(),
                FloorCount.values()[0].name(),
                CongestionLevel.values()[0].name()
        );

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/report")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("카페 제보 실패 - 필수값 누락")
    void t3() throws Exception {

        Cookie accessToken = loginAndGetAccessToken();

        // name, address 누락
        String requestBody = """
                {
                    "latitude": 37.123456,
                    "longitude": 127.123456,
                    "type": "%s",
                    "franchise": "%s",
                    "hasToilet": true,
                    "hasOutlet": true,
                    "hasWifi": true,
                    "floorCount": "%s",
                    "hasSeparateSpace": false,
                    "congestionLevel": "%s"
                }
                """.formatted(
                CafeType.values()[0].name(),
                Franchise.values()[0].name(),
                FloorCount.values()[0].name(),
                CongestionLevel.values()[0].name()
        );

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/report")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("카페 제보 실패 - 위도/경도 누락")
    void t4() throws Exception {

        Cookie accessToken = loginAndGetAccessToken();

        // latitude, longitude 누락
        String requestBody = """
                {
                    "name": "스터디 카페",
                    "address": "서울시 강남구 테헤란로 1",
                    "type": "%s",
                    "franchise": "%s",
                    "hasToilet": true,
                    "hasOutlet": true,
                    "hasWifi": true,
                    "floorCount": "%s",
                    "hasSeparateSpace": false,
                    "congestionLevel": "%s"
                }
                """.formatted(
                CafeType.values()[0].name(),
                Franchise.values()[0].name(),
                FloorCount.values()[0].name(),
                CongestionLevel.values()[0].name()
        );

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/report")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("카페 제보 실패 - 요청 바디 없음")
    void t5() throws Exception {

        Cookie accessToken = loginAndGetAccessToken();

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/report")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest());
    }
}