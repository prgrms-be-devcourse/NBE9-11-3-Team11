package com.back.team11.domain.wishlist.controller;

import com.back.team11.domain.cafe.entity.*;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.MemberRole;
import com.back.team11.domain.member.repository.MemberRepository;
import com.back.team11.domain.wishlist.entity.Wishlist;
import com.back.team11.domain.wishlist.repository.WishlistRepository;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class WishlistControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cafe savedCafe;
    private Member savedUser;

    @BeforeEach
    void setUp() {
        // 사용자 계정 생성
        if (memberRepository.findByEmail("user1@test.com").isEmpty()) {
            Member user = new Member();
            user.setEmail("user1@test.com");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setNickname("사용자1");
            user.setRole(MemberRole.ADMIN);
            memberRepository.save(user);
        }
        savedUser = memberRepository.findByEmail("user1@test.com").get();

        // 테스트용 카페 생성 (APPROVED)
        Cafe cafe = Cafe.createByAdmin(
                "테스트 카페",
                "서울시 강남구 테헤란로 1",
                new BigDecimal("37.500000"),
                new BigDecimal("127.000000"),
                "02-1234-5678",
                "조용한 카페",
                CafeType.values()[0],
                Franchise.values()[0],
                true, true, true,
                FloorCount.values()[0],
                false,
                CongestionLevel.values()[0],
                null
        );
        savedCafe = cafeRepository.save(cafe);

        memberRepository.flush();
        cafeRepository.flush();
    }

    // 헬퍼: 로그인 후 accessToken 반환
    private Cookie loginAndGetAccessToken(String email) throws Exception {
        String requestBody = """
                {
                    "email": "%s",
                    "password": "1234"
                }
                """.formatted(email);
        MvcResult result = mvc.perform(
                post("/api/V1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andReturn();
        return result.getResponse().getCookie("accessToken");
    }


    @Test
    @DisplayName("찜 추가 성공")
    void t1() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/wishlist", savedCafe.getId())
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("찜이 추가되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.data.cafeId").value(savedCafe.getId()))
                .andExpect(jsonPath("$.data.cafeName").value("테스트 카페"))
                .andExpect(jsonPath("$.data.wishlistId").exists());
    }

    @Test
    @DisplayName("찜 추가 실패 - 인증 없음")
    void t2() throws Exception {
        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/wishlist", savedCafe.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("찜 추가 실패 - 존재하지 않는 카페")
    void t3() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/wishlist", 99999L)
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("찜 추가 실패 - 이미 찜한 카페")
    void t4() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        // 미리 찜 저장
        wishlistRepository.save(Wishlist.create(savedUser, savedCafe));
        wishlistRepository.flush();

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/wishlist", savedCafe.getId())
                                .cookie(accessToken)
                )
                .andDo(print());

        // WishlistService에서 REVIEW_ALREADY_EXISTS 사용 중 → 409-3
        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-3"));
    }

    @Test
    @DisplayName("찜 취소 성공")
    void t5() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        // 미리 찜 저장
        wishlistRepository.save(Wishlist.create(savedUser, savedCafe));
        wishlistRepository.flush();

        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/wishlist", savedCafe.getId())
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("찜이 취소되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("찜 취소 실패 - 인증 없음")
    void t6() throws Exception {
        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/wishlist", savedCafe.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("찜 취소 실패 - 존재하지 않는 카페")
    void t7() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/wishlist", 99999L)
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("찜 취소 실패 - 찜하지 않은 카페")
    void t8() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        // 찜 없이 바로 취소 시도
        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/wishlist", savedCafe.getId())
                                .cookie(accessToken)
                )
                .andDo(print());

        // WishlistService에서 REVIEW_NOT_FOUND 사용 중 → 404-4
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-4"));
    }


    @Test
    @DisplayName("찜 목록 조회 성공")
    void t9() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        // 미리 찜 저장
        wishlistRepository.save(Wishlist.create(savedUser, savedCafe));
        wishlistRepository.flush();

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/member/me/wishlist")
                                .cookie(accessToken)
                                .param("page", "0")
                                .param("size", "10")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("찜 목록 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].cafeId").value(savedCafe.getId()))
                .andExpect(jsonPath("$.data.content[0].cafeName").value("테스트 카페"));
    }

    @Test
    @DisplayName("찜 목록 조회 성공 - 찜이 없으면 빈 배열")
    void t10() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/member/me/wishlist")
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("찜 목록 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("찜 목록 조회 실패 - 인증 없음")
    void t11() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/V1/member/me/wishlist")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }
}