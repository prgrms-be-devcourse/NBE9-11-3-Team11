package com.back.team11.domain.cafe.controller

import com.back.team11.domain.cafe.entity.*
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CafeSearchControllerTest {

    // @Autowired 필드는 lateinit var로 선언 - 주입 시점이 생성자 이후라 초기값 없이 선언
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var cafeRepository: CafeRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var savedCafe: Cafe

    @BeforeEach
    fun setUp() { // Java: void setUp() throws Exception → Kotlin: throws Exception 선언 불필요
        // 카페 제보자용 멤버 생성
        val member = if (memberRepository.findByEmail("user@test.com") == null) {
            // Java: new Member()로 기본 생성자 생성 후 setter로 필드 세팅
            // Kotlin: 주생성자 네임드 파라미터로 한번에 생성
            val newMember = Member(
                email = "user@test.com",
                password = passwordEncoder.encode("1234"),
                nickname = "유저",
                role = MemberRole.USER,
            )
            memberRepository.save(newMember)
        } else {
            memberRepository.findByEmail("user@test.com")!!
        }

        // 테스트용 카페 생성 (APPROVED 상태로 직접 저장)
        val cafe = Cafe.createByUser(
            member = member,
            name = "테스트 카페",
            address = "서울시 강남구 테헤란로 1",
            latitude = BigDecimal("37.500000"),
            longitude = BigDecimal("127.000000"),
            phone = "02-1234-5678",
            description = "조용한 카페",
            type = CafeType.entries[0],
            franchise = Franchise.entries[0],
            hasToilet = true,
            hasOutlet = true,
            hasWifi = true,
            floorCount = FloorCount.entries[0],
            hasSeparateSpace = false,
            congestionLevel = CongestionLevel.entries[0],
            imageUrl = "https://example.com/image.jpg",
        )
        cafe.approve() // APPROVED 상태로 변경
        savedCafe = cafeRepository.save(cafe)

        memberRepository.flush()
        cafeRepository.flush()
    }

    @Test
    @DisplayName("카페 목록 조회 성공 - 좌표 범위 포함")
    fun t1() {
        mvc.perform(
            get("/api/V1/cafe")
                .param("swLat", "37.0")
                .param("swLng", "126.5")
                .param("neLat", "38.0")
                .param("neLng", "127.5")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.data").isArray())
    }

    @Test
    @DisplayName("카페 목록 조회 성공 - 조건 없이 전체 조회")
    fun t2() {
        mvc.perform(get("/api/V1/cafe"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.data").isArray())
    }

    @Test
    @DisplayName("카페 목록 조회 성공 - 필터 조건 포함")
    fun t3() {
        mvc.perform(
            get("/api/V1/cafe")
                .param("hasWifi", "true")
                .param("hasOutlet", "true")
                .param("hasToilet", "true")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 목록 조회 성공"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.data").isArray())
    }

    @Test
    @DisplayName("카페 목록 조회 실패 - 유효하지 않은 좌표")
    fun t4() {
        mvc.perform(
            get("/api/V1/cafe")
                .param("swLat", "999.0") // 범위 초과
                .param("swLng", "126.5")
                .param("neLat", "38.0")
                .param("neLng", "127.5")
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("카페 상세 조회 성공")
    fun t5() {
        mvc.perform(get("/api/V1/cafe/${savedCafe.id}"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("카페 조회 성공"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.data.cafe.cafeId").value(savedCafe.id))
            .andExpect(jsonPath("$.data.cafe.name").value("테스트 카페"))
            .andExpect(jsonPath("$.data.cafe.address").value("서울시 강남구 테헤란로 1"))
            .andExpect(jsonPath("$.data.wishlistCount").value(0))
            .andExpect(jsonPath("$.data.createdAt").exists())
    }

    @Test
    @DisplayName("카페 상세 조회 실패 - 존재하지 않는 카페")
    fun t6() {
        mvc.perform(get("/api/V1/cafe/99999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-3"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 카페입니다."))
    }
}