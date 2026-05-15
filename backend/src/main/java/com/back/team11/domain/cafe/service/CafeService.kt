package com.back.team11.domain.cafe.service

import com.back.team11.domain.cafe.dto.CafeDetailResponse
import com.back.team11.domain.cafe.dto.CafeDetailResponse.Companion.from
import com.back.team11.domain.cafe.dto.CafeListResponse
import com.back.team11.domain.cafe.dto.CafeRequest
import com.back.team11.domain.cafe.dto.CafeResponse
import com.back.team11.domain.cafe.dto.CafeResponse.Companion.from
import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.cafe.entity.Cafe.Companion.createByUser
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.cafe.repository.CafeSearchCondition
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.domain.wishlist.repository.WishlistRepository
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.util.AuthUtil
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
@Transactional(readOnly = true)
class CafeService(
    private val cafeRepository: CafeRepository,
    private val wishlistRepository: WishlistRepository,
    private val memberRepository: MemberRepository,
    private val authUtil: AuthUtil,
) {
    // 카페 목록 조회 (지역 검색 + 필터링)
    fun searchCafes(condition: CafeSearchCondition): List<CafeListResponse> =
        cafeRepository.searchCafes(condition).map { cafe ->
            val wishlistCount = wishlistRepository.countByCafeId(cafe.id)
            CafeListResponse.from(cafe, wishlistCount)
        }

    // 카페 상세보기
    fun getCafe(cafeId: Long): CafeDetailResponse {
        val cafe = cafeRepository.findById(cafeId)
            .orElseThrow { CustomException(ErrorCode.CAFE_NOT_FOUND) }
        val wishlistCount = wishlistRepository.countByCafeId(cafeId)
        val memberId = authUtil.currentMemberIdOrNull
        val isWishlisted = memberId != null &&
                wishlistRepository.existsByMemberIdAndCafeId(memberId, cafeId)

        return CafeDetailResponse.from(cafe, wishlistCount, isWishlisted)
    }

    /**
     * 사용자 - 카페 정보 생성 요청(제보) (POST /api/V1/cafe/report)
     * 로그인한 사용자 정보를 member 필드에 연결, status는 PENDING으로 저장
     */
    @Transactional
    fun reportCafe(memberId: Long, request: CafeRequest): CafeResponse {
        // 로그인한 사용자 조회
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }

        val cafe = Cafe.createByUser(
            member = member,
            name = request.name,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            phone = request.phone,
            description = request.description,
            type = request.type,
            franchise = request.franchise,
            hasToilet = request.hasToilet,
            hasOutlet = request.hasOutlet,
            hasWifi = request.hasWifi,
            floorCount = request.floorCount,
            hasSeparateSpace = request.hasSeparateSpace,
            congestionLevel = request.congestionLevel,
            imageUrl = request.imageUrl,
        )

        return CafeResponse.from(cafeRepository.save(cafe))
    }
}