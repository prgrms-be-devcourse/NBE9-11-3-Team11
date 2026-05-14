package com.back.team11.domain.cafe.controller;

import com.back.team11.domain.cafe.entity.*;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.MemberRole;
import com.back.team11.domain.member.repository.MemberRepository;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminCafeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 관리자 계정 생성
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

    // 관리자 로그인 후 쿠키 획득
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
    @DisplayName("카페 생성 성공")
    void t1() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        String body = """
                {
                    "name": "테스트카페",
                    "address": "서울시 강남구",
                    "latitude": 37.123456,
                    "longitude": 127.123456,
                    "phone": "010-1234-5678",
                    "description": "좋은 카페",
                    "type": "FRANCHISE",
                    "franchise": "STARBUCKS",
                    "hasToilet": true,
                    "hasOutlet": true,
                    "hasWifi": true,
                    "floorCount": "ONE",
                    "hasSeparateSpace": false,
                    "congestionLevel": "MEDIUM",
                    "imageUrl": "img.jpg"
                }
                """;

        mvc.perform(
                        post("/api/V1/admin/cafe/post")
                                .cookie(accessToken)
                                .contentType("application/json")
                                .content(body)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("카페 등록 성공"))
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.data.cafe.name").value("테스트카페"))
                .andExpect(jsonPath("$.data.cafe.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.data.cafe.cafeId").exists())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }


    @Test
    @DisplayName("카페 생성 실패 - 타입 불일치")
    void t2() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        String body = """
                {
                    "name": "테스트카페",
                    "address": "서울",
                    "latitude": 37.1,
                    "longitude": 127.1,
                    "type": "INDIVIDUAL",
                    "franchise": "STARBUCKS",
                    "hasToilet": true,
                    "hasOutlet": true,
                    "hasWifi": true,
                    "floorCount": "ONE",
                    "hasSeparateSpace": false,
                    "congestionLevel": "LOW"
                }
                """;

        mvc.perform(
                        post("/api/V1/admin/cafe/post")
                                .cookie(accessToken)
                                .contentType("application/json")
                                .content(body)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }


    @Test
    @DisplayName("카페 목록 조회 성공")
    void t3() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        mvc.perform(
                        get("/api/V1/admin/cafes")
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    @DisplayName("카페 상세 조회 성공")
    void t4() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        MvcResult result = mvc.perform(
                post("/api/V1/admin/cafe/post")
                        .cookie(accessToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "카페1",
                                    "address": "서울",
                                    "latitude": 37.1,
                                    "longitude": 127.1,
                                    "type": "FRANCHISE",
                                    "franchise": "STARBUCKS",
                                    "hasToilet": true,
                                    "hasOutlet": true,
                                    "hasWifi": true,
                                    "floorCount": "ONE",
                                    "hasSeparateSpace": false,
                                    "congestionLevel": "LOW"
                                }
                                """)
        ).andReturn();

        // AdminCafeResponse 구조: { data: { cafe: { cafeId, ... }, status, createdAt } }
        Long cafeId = Long.valueOf(
                JsonPath.read(result.getResponse().getContentAsString(), "$.data.cafe.cafeId").toString()
        );

        mvc.perform(
                        get("/api/V1/admin/cafe/" + cafeId)
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cafe.cafeId").value(cafeId))
                .andExpect(jsonPath("$.data.cafe.name").value("카페1"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("카페 수정 성공")
    void t5() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        MvcResult result = mvc.perform(
                post("/api/V1/admin/cafe/post")
                        .cookie(accessToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "카페1",
                                    "address": "서울",
                                    "latitude": 37.1,
                                    "longitude": 127.1,
                                    "type": "FRANCHISE",
                                    "franchise": "STARBUCKS",
                                    "hasToilet": true,
                                    "hasOutlet": true,
                                    "hasWifi": true,
                                    "floorCount": "ONE",
                                    "hasSeparateSpace": false,
                                    "congestionLevel": "LOW"
                                }
                                """)
        ).andReturn();

        Long cafeId = Long.valueOf(
                JsonPath.read(result.getResponse().getContentAsString(), "$.data.cafe.cafeId").toString()
        );

        mvc.perform(
                        patch("/api/V1/admin/cafe/" + cafeId)
                                .cookie(accessToken)
                                .contentType("application/json")
                                .content("""
                                        {
                                            "name": "수정카페"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cafe.name").value("수정카페"))
                .andExpect(jsonPath("$.data.cafe.cafeId").value(cafeId));
    }

    @Test
    @DisplayName("카페 삭제 성공")
    void t6() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        MvcResult result = mvc.perform(
                post("/api/V1/admin/cafe/post")
                        .cookie(accessToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "카페1",
                                    "address": "서울",
                                    "latitude": 37.1,
                                    "longitude": 127.1,
                                    "type": "FRANCHISE",
                                    "franchise": "STARBUCKS",
                                    "hasToilet": true,
                                    "hasOutlet": true,
                                    "hasWifi": true,
                                    "floorCount": "ONE",
                                    "hasSeparateSpace": false,
                                    "congestionLevel": "LOW"
                                }
                                """)
        ).andReturn();

        Long cafeId = Long.valueOf(
                JsonPath.read(result.getResponse().getContentAsString(), "$.data.cafe.cafeId").toString()
        );

        mvc.perform(
                        delete("/api/V1/admin/cafe/" + cafeId)
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 삭제 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"));
    }


    @Test
    @DisplayName("카페 승인 성공")
    void t7() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        // 직접 PENDING 상태 카페 생성
        Cafe cafe = cafeRepository.save(
                Cafe.builder()
                        .name("카페1")
                        .address("서울")
                        .latitude(BigDecimal.valueOf(37.1))
                        .longitude(BigDecimal.valueOf(127.1))
                        .type(CafeType.FRANCHISE)
                        .franchise(Franchise.STARBUCKS)
                        .hasToilet(true)
                        .hasOutlet(true)
                        .hasWifi(true)
                        .floorCount(FloorCount.ONE)
                        .hasSeparateSpace(false)
                        .congestionLevel(CongestionLevel.LOW)
                        .status(CafeStatus.PENDING)
                        .build()
        );

        mvc.perform(
                        patch("/api/V1/admin/cafe/" + cafe.getId() + "/approve")
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 승인 성공"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
    @Test
    @DisplayName("카페 승인 거절 성공")
    void t8() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        // PENDING 상태 카페 생성 (거절 대상)
        Cafe cafe = cafeRepository.save(
                Cafe.builder()
                        .name("카페2")
                        .address("서울")
                        .latitude(BigDecimal.valueOf(37.2))
                        .longitude(BigDecimal.valueOf(127.2))
                        .type(CafeType.FRANCHISE)
                        .franchise(Franchise.STARBUCKS)
                        .hasToilet(true)
                        .hasOutlet(true)
                        .hasWifi(true)
                        .floorCount(FloorCount.ONE)
                        .hasSeparateSpace(false)
                        .congestionLevel(CongestionLevel.LOW)
                        .status(CafeStatus.PENDING)
                        .build()
        );

        mvc.perform(
                        patch("/api/V1/admin/cafe/" + cafe.getId() + "/reject")
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 승인 거부 성공"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("카페 생성 실패 - 인증 없음")
    void t9() throws Exception {

        String body = """
            {
                "name": "테스트카페",
                "address": "서울",
                "latitude": 37.1,
                "longitude": 127.1,
                "type": "FRANCHISE",
                "franchise": "STARBUCKS",
                "hasToilet": true,
                "hasOutlet": true,
                "hasWifi": true,
                "floorCount": "ONE",
                "hasSeparateSpace": false,
                "congestionLevel": "LOW"
            }
            """;

        mvc.perform(
                        post("/api/V1/admin/cafe/post")
                                .contentType("application/json")
                                .content(body)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }
    @Test
    @DisplayName("카페 생성 실패 - 필수값 누락")
    void t10() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        String body = """
            {
                "address": "서울"
            }
            """;

        mvc.perform(
                        post("/api/V1/admin/cafe/post")
                                .cookie(accessToken)
                                .contentType("application/json")
                                .content(body)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("카페 상세 조회 실패 - 존재하지 않음")
    void t11() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        mvc.perform(
                        get("/api/V1/admin/cafe/9999")
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("카페 수정 실패 - 존재하지 않음")
    void t12() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        mvc.perform(
                        patch("/api/V1/admin/cafe/9999")
                                .cookie(accessToken)
                                .contentType("application/json")
                                .content("""
                                    {
                                        "name": "수정"
                                    }
                                    """)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("카페 삭제 실패 - 존재하지 않음")
    void t13() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        mvc.perform(
                        delete("/api/V1/admin/cafe/9999")
                                .cookie(accessToken)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("카페 목록 조회 실패 - 인증 없음")
    void t14() throws Exception {

        mvc.perform(
                        get("/api/V1/admin/cafes")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("카페 수정 실패 - 타입 불일치")
    void t15() throws Exception {

        Cookie accessToken = loginAsAdmin().getResponse().getCookie("accessToken");

        MvcResult result = mvc.perform(
                post("/api/V1/admin/cafe/post")
                        .cookie(accessToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "카페1",
                                    "address": "서울",
                                    "latitude": 37.1,
                                    "longitude": 127.1,
                                    "type": "FRANCHISE",
                                    "franchise": "STARBUCKS",
                                    "hasToilet": true,
                                    "hasOutlet": true,
                                    "hasWifi": true,
                                    "floorCount": "ONE",
                                    "hasSeparateSpace": false,
                                    "congestionLevel": "LOW"
                                }
                                """)
        ).andReturn();

        Long cafeId = Long.valueOf(
                JsonPath.read(result.getResponse().getContentAsString(), "$.data.cafe.cafeId").toString()
        );

        mvc.perform(
                        patch("/api/V1/admin/cafe/" + cafeId)
                                .cookie(accessToken)
                                .contentType("application/json")
                                .content("""
                                        {
                                            "type": "INDIVIDUAL",
                                            "franchise": "STARBUCKS"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }
}