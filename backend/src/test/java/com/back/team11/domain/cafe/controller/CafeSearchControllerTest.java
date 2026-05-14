package com.back.team11.domain.cafe.controller;

import com.back.team11.domain.cafe.entity.*;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.MemberRole;
import com.back.team11.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CafeSearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cafe savedCafe;

    @BeforeEach
    void setUp() {
        // 카페 제보자용 멤버 생성
        Member member;
        if (memberRepository.findByEmail("user@test.com").isEmpty()) {
            member = new Member();
            member.setEmail("user@test.com");
            member.setPassword(passwordEncoder.encode("1234"));
            member.setNickname("유저");
            member.setRole(MemberRole.USER);
            memberRepository.save(member);
        } else {
            member = memberRepository.findByEmail("user@test.com").get();
        }

        // 테스트용 카페 생성 (APPROVED 상태로 직접 저장)
        Cafe cafe = Cafe.createByUser(
                member,
                "테스트 카페",
                "서울시 강남구 테헤란로 1",
                new BigDecimal("37.500000"),
                new BigDecimal("127.000000"),
                "02-1234-5678",
                "조용한 카페",
                CafeType.values()[0],
                Franchise.values()[0],
                true,
                true,
                true,
                FloorCount.values()[0],
                false,
                CongestionLevel.values()[0],
                "https://example.com/image.jpg"
        );
        cafe.approve(); // APPROVED 상태로 변경 (카페 엔티티에 approve() 메서드가 있다고 가정)
        savedCafe = cafeRepository.save(cafe);

        memberRepository.flush();
        cafeRepository.flush();
    }

    @Test
    @DisplayName("카페 목록 조회 성공 - 좌표 범위 포함")
    void t1() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe")
                                .param("swLat", "37.0")
                                .param("swLng", "126.5")
                                .param("neLat", "38.0")
                                .param("neLng", "127.5")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("카페 목록 조회 성공 - 조건 없이 전체 조회")
    void t2() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("카페 목록 조회 성공 - 필터 조건 포함")
    void t3() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe")
                                .param("hasWifi", "true")
                                .param("hasOutlet", "true")
                                .param("hasToilet", "true")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("카페 목록 조회 실패 - 유효하지 않은 좌표")
    void t4() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe")
                                .param("swLat", "999.0")   // 범위 초과
                                .param("swLng", "126.5")
                                .param("neLat", "38.0")
                                .param("neLng", "127.5")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("카페 상세 조회 성공")
    void t5() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe/{cafeId}", savedCafe.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("카페 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.cafe.cafeId").value(savedCafe.getId()))
                .andExpect(jsonPath("$.data.cafe.name").value("테스트 카페"))
                .andExpect(jsonPath("$.data.cafe.address").value("서울시 강남구 테헤란로 1"))
                .andExpect(jsonPath("$.data.wishlistCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("카페 상세 조회 실패 - 존재하지 않는 카페")
    void t6() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe/{cafeId}", 99999L)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 카페입니다."));
    }
}