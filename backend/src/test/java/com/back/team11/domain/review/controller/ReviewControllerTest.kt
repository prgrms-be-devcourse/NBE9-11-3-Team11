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
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val memberRepository: MemberRepository? = null

    @Autowired
    private val cafeRepository: CafeRepository? = null

    @Autowired
    private val reviewRepository: ReviewRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    private var savedCafe: Cafe? = null
    private var savedUser: Member? = null // 리뷰 작성자 (일반 사용자 역할 - ADMIN 계정 사용)
    private var savedAdmin: Member? = null // 관리자 (다른 사람 리뷰 삭제 테스트용)

    @BeforeEach
    fun setUp() {
        // 사용자1 (리뷰 작성자) - ADMIN 계정으로 토큰 발급
        if (memberRepository!!.findByEmail("user1@test.com").isEmpty()) {
            val user1 = Member()
            user1.setEmail("user1@test.com")
            user1.setPassword(passwordEncoder!!.encode("1234"))
            user1.setNickname("사용자1")
            user1.setRole(MemberRole.ADMIN) // 토큰 발급을 위해 ADMIN 사용
            memberRepository.save(user1)
        }
        savedUser = memberRepository.findByEmail("user1@test.com").get()

        // 사용자2 (다른 사용자 - 권한 없는 수정/삭제 테스트용)
        if (memberRepository.findByEmail("user2@test.com").isEmpty()) {
            val user2 = Member()
            user2.setEmail("user2@test.com")
            user2.setPassword(passwordEncoder!!.encode("1234"))
            user2.setNickname("사용자2")
            user2.setRole(MemberRole.ADMIN) // 토큰 발급을 위해 ADMIN 사용
            memberRepository.save(user2)
        }

        // 관리자 (다른 사람 리뷰 삭제 가능 테스트용)
        if (memberRepository.findByEmail("admin@test.com").isEmpty()) {
            val admin = Member()
            admin.setEmail("admin@test.com")
            admin.setPassword(passwordEncoder!!.encode("1234"))
            admin.setNickname("관리자")
            admin.setRole(MemberRole.ADMIN)
            memberRepository.save(admin)
        }
        savedAdmin = memberRepository.findByEmail("admin@test.com").get()

        // 테스트용 카페 (APPROVED)
        val cafe = Cafe.createByAdmin(
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
        savedCafe = cafeRepository!!.save(cafe)

        memberRepository.flush()
        cafeRepository.flush()
    }

    // 헬퍼: 로그인 후 accessToken 반환
    @Throws(Exception::class)
    private fun loginAndGetAccessToken(email: String?): Cookie? {
        val requestBody = """
            {
                "email": "$email",
                "password": "1234"
            }
        """.trimIndent()

        // 로그인 API 호출
        val result = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // 응답에서 accessToken 쿠키 꺼내기
        return result.getResponse().getCookie("accessToken")
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    @Throws(Exception::class)
    fun t1() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val requestBody = """
                { "content": "정말 좋은 카페입니다!" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe!!.getId())
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        // 응답 검증
        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 작성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("정말 좋은 카페입니다!"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.cafeId").value(savedCafe!!.getId()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 인증 없음")
    @Throws(Exception::class)
    fun t2() {
        val requestBody = """
                { "content": "정말 좋은 카페입니다!" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe!!.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 내용 없음")
    @Throws(Exception::class)
    fun t3() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val requestBody = """
                { "content": "" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe!!.getId())
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 카페")
    @Throws(Exception::class)
    fun t4() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val requestBody = """
                { "content": "정말 좋은 카페입니다!" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", 99999L)
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 중복 리뷰")
    @Throws(Exception::class)
    fun t5() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        // 미리 리뷰 저장
        reviewRepository!!.save(Review(savedUser!!, savedCafe!!, "먼저 작성한 리뷰"))
        reviewRepository.flush()

        val requestBody = """
                { "content": "중복 리뷰 시도" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/reviews", savedCafe!!.getId())
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isConflict())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("409-3"))
    }


    @Test
    @DisplayName("리뷰 목록 조회 성공 - 인증 없어도 가능")
    @Throws(Exception::class)
    fun t6() {
        reviewRepository!!.save(Review(savedUser!!, savedCafe!!, "첫 번째 리뷰"))
        reviewRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/cafe/{cafeId}/reviews", savedCafe!!.getId())
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰 목록 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].content").value("첫 번째 리뷰"))
    }

    @Test
    @DisplayName("리뷰 목록 조회 실패 - 존재하지 않는 카페")
    @Throws(Exception::class)
    fun t7() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/cafe/{cafeId}/reviews", 99999L)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("리뷰 페이징 조회 성공 - 인증 없어도 가능")
    @Throws(Exception::class)
    fun t8() {
        reviewRepository!!.save(Review(savedUser!!, savedCafe!!, "페이징 테스트 리뷰"))
        reviewRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/cafe/{cafeId}/reviews/page", savedCafe!!.getId())
                .param("page", "0")
                .param("size", "10")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰 페이징 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isArray())
    }


    @Test
    @DisplayName("리뷰 수정 성공 - 본인 리뷰")
    @Throws(Exception::class)
    fun t9() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val review = reviewRepository!!.save<Review>(Review(savedUser!!, savedCafe!!, "원래 리뷰"))
        reviewRepository.flush()

        val requestBody = """
                { "content": "수정된 리뷰" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.put(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), review.id
            )
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("수정된 리뷰"))
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 본인 리뷰 아님 (403)")
    @Throws(Exception::class)
    fun t10() {
        // user2로 로그인 후 user1의 리뷰 수정 시도
        val accessToken = loginAndGetAccessToken("user2@test.com")

        val review = reviewRepository!!.save<Review>(Review(savedUser!!, savedCafe!!, "user1의 리뷰"))
        reviewRepository.flush()

        val requestBody = """
                { "content": "user2가 수정 시도" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.put(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), review.id
            )
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-2"))
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    @Throws(Exception::class)
    fun t11() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val requestBody = """
                { "content": "수정 시도" }
                
                """.trimIndent()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.put(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), 99999L
            )
                .cookie(accessToken!!)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-4"))
    }


    @Test
    @DisplayName("리뷰 삭제 성공 - 본인 리뷰")
    @Throws(Exception::class)
    fun t12() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val review = reviewRepository!!.save<Review>(Review(savedUser!!, savedCafe!!, "삭제할 리뷰"))
        reviewRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), review.id
            )
                .cookie(accessToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 삭제되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 관리자가 다른 사람 리뷰 삭제")
    @Throws(Exception::class)
    fun t13() {
        // user1의 리뷰를 admin이 삭제
        val adminAccessToken = loginAndGetAccessToken("admin@test.com")

        val review = reviewRepository!!.save<Review>(Review(savedUser!!, savedCafe!!, "user1의 리뷰"))
        reviewRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), review.id
            )
                .cookie(adminAccessToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("리뷰가 삭제되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
    @Throws(Exception::class)
    fun t14() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), 99999L
            )
                .cookie(accessToken!!)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-4"))
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 인증 없음")
    @Throws(Exception::class)
    fun t15() {
        val review = reviewRepository!!.save<Review>(Review(savedUser!!, savedCafe!!, "삭제 시도 리뷰"))
        reviewRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete(
                "/api/V1/cafe/{cafeId}/reviews/{reviewId}",
                savedCafe!!.getId(), review.id
            )
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }
}