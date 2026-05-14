package com.back.team11.domain.review.controller;

import com.back.team11.domain.cafe.entity.*;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.MemberRole;
import com.back.team11.domain.member.repository.MemberRepository;
import com.back.team11.domain.review.entity.Review;
import com.back.team11.domain.review.repository.ReviewRepository;
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
class ReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cafe savedCafe;
    private Member savedUser;   // 리뷰 작성자 (일반 사용자 역할 - ADMIN 계정 사용)
    private Member savedAdmin;  // 관리자 (다른 사람 리뷰 삭제 테스트용)

    @BeforeEach
    void setUp() {
        // 사용자1 (리뷰 작성자) - ADMIN 계정으로 토큰 발급
        if (memberRepository.findByEmail("user1@test.com").isEmpty()) {
            Member user1 = new Member();
            user1.setEmail("user1@test.com");
            user1.setPassword(passwordEncoder.encode("1234"));
            user1.setNickname("사용자1");
            user1.setRole(MemberRole.ADMIN); // 토큰 발급을 위해 ADMIN 사용
            memberRepository.save(user1);
        }
        savedUser = memberRepository.findByEmail("user1@test.com").get();

        // 사용자2 (다른 사용자 - 권한 없는 수정/삭제 테스트용)
        if (memberRepository.findByEmail("user2@test.com").isEmpty()) {
            Member user2 = new Member();
            user2.setEmail("user2@test.com");
            user2.setPassword(passwordEncoder.encode("1234"));
            user2.setNickname("사용자2");
            user2.setRole(MemberRole.ADMIN); // 토큰 발급을 위해 ADMIN 사용
            memberRepository.save(user2);
        }

        // 관리자 (다른 사람 리뷰 삭제 가능 테스트용)
        if (memberRepository.findByEmail("admin@test.com").isEmpty()) {
            Member admin = new Member();
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setNickname("관리자");
            admin.setRole(MemberRole.ADMIN);
            memberRepository.save(admin);
        }
        savedAdmin = memberRepository.findByEmail("admin@test.com").get();

        // 테스트용 카페 (APPROVED)
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

        // 로그인 API 호출
        MvcResult result = mvc.perform(
                post("/api/V1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andReturn();

        // 응답에서 accessToken 쿠키 꺼내기
        return result.getResponse().getCookie("accessToken");
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    void t1() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        String requestBody = """
                { "content": "정말 좋은 카페입니다!" }
                """;

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/reviews", savedCafe.getId())
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        // 응답 검증
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("리뷰가 작성되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.data.content").value("정말 좋은 카페입니다!"))
                .andExpect(jsonPath("$.data.cafeId").value(savedCafe.getId()))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 인증 없음")
    void t2() throws Exception {
        String requestBody = """
                { "content": "정말 좋은 카페입니다!" }
                """;

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/reviews", savedCafe.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 내용 없음")
    void t3() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        String requestBody = """
                { "content": "" }
                """;

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/reviews", savedCafe.getId())
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
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 카페")
    void t4() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        String requestBody = """
                { "content": "정말 좋은 카페입니다!" }
                """;

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/reviews", 99999L)
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 중복 리뷰")
    void t5() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        // 미리 리뷰 저장
        reviewRepository.save(new Review(savedUser, savedCafe, "먼저 작성한 리뷰"));
        reviewRepository.flush();

        String requestBody = """
                { "content": "중복 리뷰 시도" }
                """;

        ResultActions resultActions = mvc.perform(
                        post("/api/V1/cafe/{cafeId}/reviews", savedCafe.getId())
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-3"));
    }


    @Test
    @DisplayName("리뷰 목록 조회 성공 - 인증 없어도 가능")
    void t6() throws Exception {
        reviewRepository.save(new Review(savedUser, savedCafe, "첫 번째 리뷰"));
        reviewRepository.flush();

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe/{cafeId}/reviews", savedCafe.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].content").value("첫 번째 리뷰"));
    }

    @Test
    @DisplayName("리뷰 목록 조회 실패 - 존재하지 않는 카페")
    void t7() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe/{cafeId}/reviews", 99999L)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-3"));
    }

    @Test
    @DisplayName("리뷰 페이징 조회 성공 - 인증 없어도 가능")
    void t8() throws Exception {
        reviewRepository.save(new Review(savedUser, savedCafe, "페이징 테스트 리뷰"));
        reviewRepository.flush();

        ResultActions resultActions = mvc.perform(
                        get("/api/V1/cafe/{cafeId}/reviews/page", savedCafe.getId())
                                .param("page", "0")
                                .param("size", "10")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("리뷰 페이징 조회 성공"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.content").isArray());
    }


    @Test
    @DisplayName("리뷰 수정 성공 - 본인 리뷰")
    void t9() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        Review review = reviewRepository.save(new Review(savedUser, savedCafe, "원래 리뷰"));
        reviewRepository.flush();

        String requestBody = """
                { "content": "수정된 리뷰" }
                """;

        ResultActions resultActions = mvc.perform(
                        put("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), review.getId())
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("리뷰가 수정되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.content").value("수정된 리뷰"));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 본인 리뷰 아님 (403)")
    void t10() throws Exception {
        // user2로 로그인 후 user1의 리뷰 수정 시도
        Cookie accessToken = loginAndGetAccessToken("user2@test.com");

        Review review = reviewRepository.save(new Review(savedUser, savedCafe, "user1의 리뷰"));
        reviewRepository.flush();

        String requestBody = """
                { "content": "user2가 수정 시도" }
                """;

        ResultActions resultActions = mvc.perform(
                        put("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), review.getId())
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-2"));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    void t11() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        String requestBody = """
                { "content": "수정 시도" }
                """;

        ResultActions resultActions = mvc.perform(
                        put("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), 99999L)
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-4"));
    }



    @Test
    @DisplayName("리뷰 삭제 성공 - 본인 리뷰")
    void t12() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        Review review = reviewRepository.save(new Review(savedUser, savedCafe, "삭제할 리뷰"));
        reviewRepository.flush();

        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), review.getId())
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("리뷰가 삭제되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 관리자가 다른 사람 리뷰 삭제")
    void t13() throws Exception {
        // user1의 리뷰를 admin이 삭제
        Cookie adminAccessToken = loginAndGetAccessToken("admin@test.com");

        Review review = reviewRepository.save(new Review(savedUser, savedCafe, "user1의 리뷰"));
        reviewRepository.flush();

        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), review.getId())
                                .cookie(adminAccessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("리뷰가 삭제되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
    void t14() throws Exception {
        Cookie accessToken = loginAndGetAccessToken("user1@test.com");

        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), 99999L)
                                .cookie(accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-4"));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 인증 없음")
    void t15() throws Exception {
        Review review = reviewRepository.save(new Review(savedUser, savedCafe, "삭제 시도 리뷰"));
        reviewRepository.flush();

        ResultActions resultActions = mvc.perform(
                        delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                                savedCafe.getId(), review.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }
}