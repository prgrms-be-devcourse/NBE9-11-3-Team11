package com.back.team11.domain.cafe.controller

import com.back.team11.domain.cafe.entity.CafeType
import com.back.team11.domain.cafe.entity.CongestionLevel
import com.back.team11.domain.cafe.entity.FloorCount
import com.back.team11.domain.cafe.entity.Franchise
import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CafeControllerTest {

    // @Autowired 필드는 lateinit var로 선언 - 주입 시점이 생성자 이후라 초기값 없이 선언
    // JUnit이 테스트 클래스 인스턴스를 먼저 생성한 다음에 Spring이 의존성을 주입하는 순서라서 생성자 주입이 안됨
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() { // Java: void setUp() throws Exception → Kotlin: throws Exception 선언 불필요
        if (memberRepository.findByEmail("admin@test.com").isEmpty) {
            // Java: new Member()로 기본 생성자 생성 후 setter로 필드 세팅
// Kotlin: 기본 생성자 없이 주생성자 네임드 파라미터로 한번에 생성
            val admin = Member(
                email = "admin@test.com",
                password = passwordEncoder.encode("1234"),
                nickname = "관리자",
                role = MemberRole.ADMIN,
            )
            memberRepository.save(admin)
        }
        memberRepository.flush()
    }

    // 헬퍼: 관리자 로그인 후 accessToken 반환
    private fun loginAndGetAccessToken(): Cookie? {
        val requestBody = """
            {
                "email": "admin@test.com",
                "password": "1234"
            }
        """.trimIndent()

        val result = mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        return result.response.getCookie("accessToken")
    }

    @Test
    @DisplayName("카페 제보 성공")
    fun t1() {
        val accessToken = loginAndGetAccessToken() ?: return // Cookie? nullable 타입이라 null 안전 처리

        // Java: String.formatted() → Kotlin: trimIndent() + 문자열 템플릿
        val requestBody = """
            {
                "name": "스터디 카페",
                "address": "서울시 강남구 테헤란로 1",
                "latitude": 37.123456,
                "longitude": 127.123456,
                "phone": "02-1234-5678",
                "description": "조용한 카페입니다.",
                "type": "${CafeType.entries[0].name}",
                "franchise": "${Franchise.entries[0].name}",
                "hasToilet": true,
                "hasOutlet": true,
                "hasWifi": true,
                "floorCount": "${FloorCount.entries[0].name}",
                "hasSeparateSpace": false,
                "congestionLevel": "${CongestionLevel.entries[0].name}",
                "imageUrl": "https://example.com/image.jpg"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/V1/cafe/report")
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.msg").value("카페 제보가 접수되었습니다."))
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.data.cafe.name").value("스터디 카페"))
            .andExpect(jsonPath("$.data.cafe.address").value("서울시 강남구 테헤란로 1"))
            .andExpect(jsonPath("$.data.cafe.cafeId").exists())
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("카페 제보 실패 - 인증 없음")
    fun t2() {
        val requestBody = """
            {
                "name": "스터디 카페",
                "address": "서울시 강남구 테헤란로 1",
                "latitude": 37.123456,
                "longitude": 127.123456,
                "type": "${CafeType.entries[0].name}",
                "franchise": "${Franchise.entries[0].name}",
                "hasToilet": true,
                "hasOutlet": true,
                "hasWifi": true,
                "floorCount": "${FloorCount.entries[0].name}",
                "hasSeparateSpace": false,
                "congestionLevel": "${CongestionLevel.entries[0].name}"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/V1/cafe/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("카페 제보 실패 - 필수값 누락")
    fun t3() {
        val accessToken = loginAndGetAccessToken() ?: return

        // name, address 누락
        val requestBody = """
            {
                "latitude": 37.123456,
                "longitude": 127.123456,
                "type": "${CafeType.entries[0].name}",
                "franchise": "${Franchise.entries[0].name}",
                "hasToilet": true,
                "hasOutlet": true,
                "hasWifi": true,
                "floorCount": "${FloorCount.entries[0].name}",
                "hasSeparateSpace": false,
                "congestionLevel": "${CongestionLevel.entries[0].name}"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/V1/cafe/report")
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("카페 제보 실패 - 위도/경도 누락")
    fun t4() {
        val accessToken = loginAndGetAccessToken() ?: return

        // latitude, longitude 누락
        val requestBody = """
            {
                "name": "스터디 카페",
                "address": "서울시 강남구 테헤란로 1",
                "type": "${CafeType.entries[0].name}",
                "franchise": "${Franchise.entries[0].name}",
                "hasToilet": true,
                "hasOutlet": true,
                "hasWifi": true,
                "floorCount": "${FloorCount.entries[0].name}",
                "hasSeparateSpace": false,
                "congestionLevel": "${CongestionLevel.entries[0].name}"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/V1/cafe/report")
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("카페 제보 실패 - 요청 바디 없음")
    fun t5() {
        val accessToken = loginAndGetAccessToken() ?: return

        mvc.perform(
            post("/api/V1/cafe/report")
                .cookie(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
    }
}