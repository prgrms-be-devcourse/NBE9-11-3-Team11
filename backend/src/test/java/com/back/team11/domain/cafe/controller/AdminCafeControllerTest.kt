package com.back.team11.domain.cafe.controller

import com.back.team11.domain.cafe.entity.*
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminCafeControllerTest {

    // @Autowired 필드는 lateinit var로 선언 - 주입 시점이 생성자 이후라 초기값 없이 선언
    // JUnit이 테스트 클래스 인스턴스를 먼저 생성한 다음에 Spring이 의존성을 주입하는 순서라서 생성자 주입이 안됨
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var cafeRepository: CafeRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    // Java: void setUp() throws Exception → Kotlin: throws Exception 선언 불필요, fun으로 변경
    fun setUp() {
        // 관리자 계정 생성
        if (memberRepository.findByEmail("admin@test.com").isEmpty) {
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

    // 관리자 로그인 후 쿠키 획득
    private fun loginAsAdmin(): MvcResult {
        val requestBody = """
            {
                "email": "admin@test.com",
                "password": "1234"
            }
        """.trimIndent()

        return mvc.perform(
            post("/api/V1/admin/auth/login")
                .contentType("application/json")
                .content(requestBody)
        ).andReturn()
    }

    @Test
    @DisplayName("카페 생성 성공")
    fun t1() {
        // Cookie? nullable 타입이라 ?: return으로 null 안전 처리
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        // Cafe 생성 시 builder 대신 주생성자 네임드 파라미터 사용
        val body = """
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
        """.trimIndent()

        mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
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
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("카페 생성 실패 - 타입 불일치")
    fun t2() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        val body = """
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
        """.trimIndent()

        mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
                .contentType("application/json")
                .content(body)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("카페 목록 조회 성공")
    fun t3() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        mvc.perform(
            get("/api/V1/admin/cafes")
                .cookie(accessToken!!)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
            .andExpect(jsonPath("$.data.content").exists())
    }

    @Test
    @DisplayName("카페 상세 조회 성공")
    fun t4() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        val result = mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
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
                """.trimIndent())
        ).andReturn()

        val cafeId = JsonPath.read<Any>(result.response.contentAsString, "$.data.cafe.cafeId").toString().toLong()

        mvc.perform(
            get("/api/V1/admin/cafe/$cafeId")
                .cookie(accessToken)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cafe.cafeId").value(cafeId))
            .andExpect(jsonPath("$.data.cafe.name").value("카페1"))
            .andExpect(jsonPath("$.data.status").value("APPROVED"))
    }

    @Test
    @DisplayName("카페 수정 성공")
    fun t5() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        val result = mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
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
                """.trimIndent())
        ).andReturn()

        val cafeId = JsonPath.read<Any>(result.response.contentAsString, "$.data.cafe.cafeId").toString().toLong()

        mvc.perform(
            patch("/api/V1/admin/cafe/$cafeId")
                .cookie(accessToken)
                .contentType("application/json")
                .content("""{"name": "수정카페"}""")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cafe.name").value("수정카페"))
            .andExpect(jsonPath("$.data.cafe.cafeId").value(cafeId))
    }

    @Test
    @DisplayName("카페 삭제 성공")
    fun t6() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        val result = mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
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
                """.trimIndent())
        ).andReturn()

        val cafeId = JsonPath.read<Any>(result.response.contentAsString, "$.data.cafe.cafeId").toString().toLong()

        mvc.perform(
            delete("/api/V1/admin/cafe/$cafeId")
                .cookie(accessToken)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 삭제 성공"))
            .andExpect(jsonPath("$.resultCode").value("200"))
    }

    @Test
    @DisplayName("카페 승인 성공")
    fun t7() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        // 직접 PENDING 상태 카페 생성
        val cafe = cafeRepository.save(
            Cafe(
                name = "카페1",
                address = "서울",
                latitude = BigDecimal.valueOf(37.1),
                longitude = BigDecimal.valueOf(127.1),
                type = CafeType.FRANCHISE,
                franchise = Franchise.STARBUCKS,
                hasToilet = true,
                hasOutlet = true,
                hasWifi = true,
                floorCount = FloorCount.ONE,
                hasSeparateSpace = false,
                congestionLevel = CongestionLevel.LOW,
                status = CafeStatus.PENDING,
            )
        )

        mvc.perform(
            patch("/api/V1/admin/cafe/${cafe.id}/approve")
                .cookie(accessToken!!)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 승인 성공"))
            .andExpect(jsonPath("$.data.status").value("APPROVED"))
    }

    @Test
    @DisplayName("카페 승인 거절 성공")
    fun t8() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        // PENDING 상태 카페 생성 (거절 대상)
        val cafe = cafeRepository.save(
            Cafe(
                name = "카페2",
                address = "서울",
                latitude = BigDecimal.valueOf(37.2),
                longitude = BigDecimal.valueOf(127.2),
                type = CafeType.FRANCHISE,
                franchise = Franchise.STARBUCKS,
                hasToilet = true,
                hasOutlet = true,
                hasWifi = true,
                floorCount = FloorCount.ONE,
                hasSeparateSpace = false,
                congestionLevel = CongestionLevel.LOW,
                status = CafeStatus.PENDING,
            )
        )

        mvc.perform(
            patch("/api/V1/admin/cafe/${cafe.id}/reject")
                .cookie(accessToken!!)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 승인 거부 성공"))
            .andExpect(jsonPath("$.data.status").value("REJECTED"))
    }

    @Test
    @DisplayName("카페 생성 실패 - 인증 없음")
    fun t9() {
        mvc.perform(
            post("/api/V1/admin/cafe/post")
                .contentType("application/json")
                .content("""
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
                """.trimIndent())
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("카페 생성 실패 - 필수값 누락")
    fun t10() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
                .contentType("application/json")
                .content("""{"address": "서울"}""")
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("카페 상세 조회 실패 - 존재하지 않음")
    fun t11() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        mvc.perform(
            get("/api/V1/admin/cafe/9999")
                .cookie(accessToken!!)
        )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("카페 수정 실패 - 존재하지 않음")
    fun t12() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        mvc.perform(
            patch("/api/V1/admin/cafe/9999")
                .cookie(accessToken!!)
                .contentType("application/json")
                .content("""{"name": "수정"}""")
        )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("카페 삭제 실패 - 존재하지 않음")
    fun t13() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        mvc.perform(
            delete("/api/V1/admin/cafe/9999")
                .cookie(accessToken!!)
        )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-3"))
    }

    @Test
    @DisplayName("카페 목록 조회 실패 - 인증 없음")
    fun t14() {
        mvc.perform(get("/api/V1/admin/cafes"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
    }

    @Test
    @DisplayName("카페 수정 실패 - 타입 불일치")
    fun t15() {
        val accessToken = loginAsAdmin().response.getCookie("accessToken")

        val result = mvc.perform(
            post("/api/V1/admin/cafe/post")
                .cookie(accessToken!!)
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
                """.trimIndent())
        ).andReturn()

        val cafeId = JsonPath.read<Any>(result.response.contentAsString, "$.data.cafe.cafeId").toString().toLong()

        mvc.perform(
            patch("/api/V1/admin/cafe/$cafeId")
                .cookie(accessToken)
                .contentType("application/json")
                .content("""{"type": "INDIVIDUAL", "franchise": "STARBUCKS"}""")
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
    }
}