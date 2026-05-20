package com.back.team11.domain.cafe.service

import com.back.team11.domain.cafe.dto.*
import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.cafe.entity.CafeStatus
import com.back.team11.domain.cafe.entity.CafeType
import com.back.team11.domain.cafe.entity.Franchise
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.review.repository.ReviewRepository
import com.back.team11.domain.wishlist.repository.WishlistRepository
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.extension.findByIdOrThrow
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCafeService(
    private val cafeRepository: CafeRepository,
    private val wishlistRepository: WishlistRepository,
    private val reviewRepository: ReviewRepository,
) {

    /**
     * 관리자 - 카페 정보 생성 (POST /api/V1/admin/cafe/post)
     */
    @Transactional
    fun createCafe(request: CafeRequest): AdminCafeResponse {
        // type과 franchise 일관성 검증
        validateFranchiseConsistency(request.type, request.franchise)

        val cafe = Cafe.createByAdmin(
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

        return AdminCafeResponse.from(cafeRepository.save(cafe))
    }

    /**
     * 관리자 - 카페 정보 수정 (PATCH /api/V1/admin/cafe/{cafeId})
     * 전송된 필드만 수정, null인 필드는 기존값 유지
     */
    @Transactional
    fun updateCafe(cafeId: Long, request: CafeUpdateRequest): AdminCafeResponse {
        // cafeId로 카페 조회, 존재하지 않으면 예외 발생
        val cafe = cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }

        // type, franchise 둘 다 전송된 경우에만 일관성 검증
        if (request.type != null || request.franchise != null) {
            val type = request.type ?: cafe.type
            val franchise = request.franchise ?: cafe.franchise
            validateFranchiseConsistency(type, franchise)
        }

        // 더티체킹으로 UPDATE 실행 (@Transactional 범위 안에서 필드 변경 시 자동 반영)
        cafe.updateByAdmin(
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

        return AdminCafeResponse.from(cafe)
    }

    // INDIVIDUAL 타입 ↔ Franchise 브랜드 일관성 검증
    private fun validateFranchiseConsistency(type: CafeType, franchise: Franchise) {
        val hasBrand = franchise != Franchise.NONE

        if (type == CafeType.INDIVIDUAL && hasBrand) {
            // 개인 카페인데 브랜드가 선택된 경우
            throw CustomException(ErrorCode.INVALID_INPUT_VALUE)
        }

        if (type == CafeType.FRANCHISE && !hasBrand) {
            // 프랜차이즈인데 브랜드가 NONE이거나 없는 경우
            throw CustomException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    /**
     * 관리자 - 카페 목록 조회 (페이징 및 필터링)
     */
    @Transactional(readOnly = true)
    fun getCafes(condition: AdminCafeSearchCondition, page: Int): PageResponse<AdminCafeResponse> {
        // page는 클라이언트에서 1부터 들어오므로, JPA의 0-based index에 맞춰 1을 빼줌
        val pageable = PageRequest.of(page - 1, 15)

        // QueryDSL을 통해 조건에 맞는 Cafe Page 조회
        val cafePage = cafeRepository.searchAdminCafes(condition, pageable)

        // Page<Cafe>를 Page<AdminCafeResponse>로 변환 후 PageResponse 객체로 매핑
        val dtoPage = cafePage.map { AdminCafeResponse.from(it) }

        return PageResponse.of(dtoPage)
    }

    /**
     * 관리자 - 카페 상세 조회
     */
    @Transactional(readOnly = true)
    fun getCafe(cafeId: Long): AdminCafeResponse {
        val cafe = cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }
        return AdminCafeResponse.from(cafe)
    }

    /**
     * 관리자 - 카페 정보 삭제 (DELETE /api/V1/admin/cafe/{cafeId})
     * 외래키 제약 조건으로 인해 연관 데이터(찜, 리뷰)를 먼저 삭제 후 카페 삭제
     */
    @Transactional
    fun deleteCafe(cafeId: Long) {
        val cafe = cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }

        // 1. 연관된 찜 목록 먼저 삭제
        wishlistRepository.deleteByCafeId(cafeId)

        // 2. 연관된 리뷰 먼저 삭제
        reviewRepository.deleteByCafeId(cafeId)

        // 3. 카페 삭제
        cafeRepository.delete(cafe)
    }

    /**
     * 관리자 - 사용자 카페 정보 등록 - 승인 (PATCH /api/V1/admin/cafe/{cafeId}/approve)
     * 이미 승인된 카페 재승인 시 409 에러
     */
    @Transactional
    fun approveCafe(cafeId: Long): AdminCafeResponse {
        val cafe = cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }

        // 이미 승인된 카페 중복 처리 방지
        if (cafe.status == CafeStatus.APPROVED) {
            throw CustomException(ErrorCode.CAFE_ALREADY_APPROVED)
        }

        // 더티체킹으로 UPDATE 실행
        cafe.approve()

        return AdminCafeResponse.from(cafe)
    }

    /**
     * 관리자 - 사용자 카페 정보 등록 - 거부 (PATCH /api/V1/admin/cafe/{cafeId}/reject)
     * 이미 거절된 카페 재거절 시 409 에러
     */
    @Transactional
    fun rejectCafe(cafeId: Long): AdminCafeResponse {
        val cafe = cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }

        // 이미 거절된 카페 중복 처리 방지
        if (cafe.status == CafeStatus.REJECTED) {
            throw CustomException(ErrorCode.CAFE_ALREADY_REJECTED)
        }

        // 더티체킹으로 UPDATE 실행
        cafe.reject()

        return AdminCafeResponse.from(cafe)
    }
}
