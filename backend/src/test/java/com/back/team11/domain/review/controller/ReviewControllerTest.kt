package com.back.team11.domain.review.controller

import com.back.team11.domain.cafe.entity.*
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.domain.review.entity.Review
import com.back.team11.domain.review.repository.ReviewRepository
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
internal class ReviewControllerTest {

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var memberRepository: MemberRepository
    @Autowired lateinit var cafeRepository: CafeRepository
    @Autowired lateinit var reviewRepository: ReviewRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder

    private lateinit var savedCafe: Cafe
    private lateinit var savedUser: Member
    private lateinit var savedAdmin: Member

    @BeforeEach
    fun setUp() {
        fun createMemberIfAbsent(email: String, nickname: String): Member {
            if (memberRepository.findByEmail(email) == null) {
                memberRepository.save(
                    Member(email = email, nickname = nickname).apply {
                        password = passwordEncoder.encode("1234")
                        role = MemberRole.ADMIN
                    }
                )
            }
            return memberRepository.findByEmail(email)!!
        }

        savedUser = createMemberIfAbsent("user1@test.com", "사용자1")
        createMemberIfAbsent("user2@test.com", "사용자2")
        savedAdmin = createMemberIfAbsent("admin@test.com", "관리자")

        savedCafe = cafeRepository.save(
            Cafe.createByAdmin(
                "테스트 카페",
                "서울시 강남구 테헤란로 1",
                BigDecimal("37.500000"),
                BigDecimal("127.000000"),
                "02-1234-5678",
                "조용한 카페",
                CafeType.entries[0],
                Franchise.entries[0],
                true, true, true,
                FloorCount.entries[0],
                false,
                CongestionLevel.entries[0],
                null
            )
        )

        memberRepository.flush()
        cafeRepository.flush()
    }

    private fun loginAndGetAccessToken(email: String): Cookie {
        val result = mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "email": "$email", "password": "1234" }""")
        ).andReturn()

        return result.response.getCookie("accessToken")!!
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    fun t1() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe.id)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "정말 좋은 카페입니다!" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 작성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("정말 좋은 카페입니다!"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.cafeId").value(savedCafe.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 인증 없음")
    fun t2() {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "정말 좋은 카페입니다!" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 내용 없음")
    fun t3() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe.id)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 카페")
    fun t4() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", 99999L)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "정말 좋은 카페입니다!" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 중복 리뷰")
    fun t5() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "먼저 작성한 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe.id)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "중복 리뷰 시도" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isConflict())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("409-3"))
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공 - 인증 없어도 가능")
    fun t6() {
        reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "첫 번째 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.get("/api/V1/cafe/{cafeId}/reviews", savedCafe.id)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰 목록 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].content").value("첫 번째 리뷰"))
    }

    @Test
    @DisplayName("리뷰 목록 조회 실패 - 존재하지 않는 카페")
    fun t7() {
        mvc.perform(
            MockMvcRequestBuilders.get("/api/V1/cafe/{cafeId}/reviews", 99999L)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("리뷰 페이징 조회 성공 - 인증 없어도 가능")
    fun t8() {
        reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "페이징 테스트 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.get("/api/V1/cafe/{cafeId}/reviews/page", savedCafe.id)
                .param("page", "0")
                .param("size", "10")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰 페이징 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isArray())
    }

    @Test
    @DisplayName("리뷰 수정 성공 - 본인 리뷰")
    fun t9() {
        val accessToken = loginAndGetAccessToken("user1@test.com")
        val review = reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "원래 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.put("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, review.id)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "수정된 리뷰" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("수정된 리뷰"))
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 본인 리뷰 아님 (403)")
    fun t10() {
        val accessToken = loginAndGetAccessToken("user2@test.com")
        val review = reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "user1의 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.put("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, review.id)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "user2가 수정 시도" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-2"))
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    fun t11() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        mvc.perform(
            MockMvcRequestBuilders.put("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, 99999L)
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "content": "수정 시도" }""")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-4"))
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 본인 리뷰")
    fun t12() {
        val accessToken = loginAndGetAccessToken("user1@test.com")
        val review = reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "삭제할 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, review.id)
                .cookie(accessToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 삭제되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 관리자가 다른 사람 리뷰 삭제")
    fun t13() {
        val adminAccessToken = loginAndGetAccessToken("admin@test.com")
        val review = reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "user1의 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, review.id)
                .cookie(adminAccessToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 삭제되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
    fun t14() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, 99999L)
                .cookie(accessToken)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-4"))
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 인증 없음")
    fun t15() {
        val review = reviewRepository.save(Review(member = savedUser, cafe = savedCafe, content = "삭제 시도 리뷰"))
        reviewRepository.flush()

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/reviews/{reviewId}", savedCafe.id, review.id)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }
}