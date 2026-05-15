package com.back.team11.domain.wishlist.controller

import com.back.team11.domain.cafe.entity.*
import com.back.team11.domain.cafe.entity.Cafe.Companion.createByAdmin
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.domain.wishlist.entity.Wishlist
import com.back.team11.domain.wishlist.repository.WishlistRepository
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
internal class WishlistControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val memberRepository: MemberRepository? = null

    @Autowired
    private val cafeRepository: CafeRepository? = null

    @Autowired
    private val wishlistRepository: WishlistRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    private var savedCafe: Cafe? = null
    private var savedUser: Member? = null

    @BeforeEach
    fun setUp() {
        // 사용자 계정 생성
        if (memberRepository!!.findByEmail("user1@test.com").isEmpty()) {
            val user = Member(
                email = "user1@test.com",
                nickname = "사용자1"
            ).apply {
                password = passwordEncoder!!.encode("1234")
                role = MemberRole.ADMIN
            }
            memberRepository.save(user)
        }
        savedUser = memberRepository.findByEmail("user1@test.com").get()

        // 테스트용 카페 생성
        val cafe = createByAdmin(
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

        val result = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()
        return result.response.getCookie("accessToken")
    }

    @Test
    @DisplayName("찜 추가 성공")
    @Throws(Exception::class)
    fun t1() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/wishlist", savedCafe!!.id)
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("찜이 추가되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.cafeId").value(savedCafe!!.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.cafeName").value("테스트 카페"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.wishlistId").exists())
    }

    @Test
    @DisplayName("찜 추가 실패 - 인증 없음")
    @Throws(Exception::class)
    fun t2() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/wishlist", savedCafe!!.id)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("찜 추가 실패 - 존재하지 않는 카페")
    @Throws(Exception::class)
    fun t3() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/wishlist", 99999L)
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("찜 추가 실패 - 이미 찜한 카페")
    @Throws(Exception::class)
    fun t4() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        // 미리 찜 저장
        wishlistRepository!!.save(Wishlist.create(savedUser!!, savedCafe!!))
        wishlistRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.post("/api/V1/cafe/{cafeId}/wishlist", savedCafe!!.id)
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        // WishlistService에서 REVIEW_ALREADY_EXISTS 사용 중 → 409-3
        resultActions
            .andExpect(MockMvcResultMatchers.status().isConflict())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("409-3"))
    }

    @Test
    @DisplayName("찜 취소 성공")
    @Throws(Exception::class)
    fun t5() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        // 미리 찜 저장
        wishlistRepository!!.save(Wishlist.create(savedUser!!, savedCafe!!))
        wishlistRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/wishlist", savedCafe!!.id)
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("찜이 취소되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
    }

    @Test
    @DisplayName("찜 취소 실패 - 인증 없음")
    @Throws(Exception::class)
    fun t6() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/wishlist", savedCafe!!.id)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("찜 취소 실패 - 존재하지 않는 카페")
    @Throws(Exception::class)
    fun t7() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/wishlist", 99999L)
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("찜 취소 실패 - 찜하지 않은 카페")
    @Throws(Exception::class)
    fun t8() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.delete("/api/V1/cafe/{cafeId}/wishlist", savedCafe!!.id)
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        // WishlistService에서 REVIEW_NOT_FOUND 사용 중 → 404-4
        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-4"))
    }

    @Test
    @DisplayName("찜 목록 조회 성공")
    @Throws(Exception::class)
    fun t9() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        // 미리 찜 저장
        wishlistRepository!!.save(Wishlist.create(savedUser!!, savedCafe!!))
        wishlistRepository.flush()

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/member/me/wishlist")
                .cookie(accessToken!!)
                .param("page", "0")
                .param("size", "10")
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("찜 목록 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].cafeId").value(savedCafe!!.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].cafeName").value("테스트 카페"))
    }

    @Test
    @DisplayName("찜 목록 조회 성공 - 찜이 없으면 빈 배열")
    @Throws(Exception::class)
    fun t10() {
        val accessToken = loginAndGetAccessToken("user1@test.com")

        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/member/me/wishlist")
                .cookie(accessToken!!)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("찜 목록 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty())
    }

    @Test
    @DisplayName("찜 목록 조회 실패 - 인증 없음")
    @Throws(Exception::class)
    fun t11() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/V1/member/me/wishlist")
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
    }
}
