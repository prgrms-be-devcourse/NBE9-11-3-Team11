package com.back.team11.domain.cafe.service;

import com.back.team11.domain.cafe.dto.*;
import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.cafe.entity.CafeStatus;
import com.back.team11.domain.cafe.entity.CafeType;
import com.back.team11.domain.cafe.entity.Franchise;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.domain.review.repository.ReviewRepository;
import com.back.team11.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCafeService {

    private final CafeRepository cafeRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;

    /**
     관리자 - 카페 정보 생성 (POST /api/V1/admin/cafe/post)
     **/
    @Transactional
    public AdminCafeResponse createCafe(CafeRequest request) {

        // type과 franchise 일관성 검증
        validateFranchiseConsistency(request.getType(), request.getFranchise());

        Cafe cafe = Cafe.createByAdmin(
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPhone(),
                request.getDescription(),
                request.getType(),
                request.getFranchise(),
                request.getHasToilet(),
                request.getHasOutlet(),
                request.getHasWifi(),
                request.getFloorCount(),
                request.getHasSeparateSpace(),
                request.getCongestionLevel(),
                request.getImageUrl()
        );

        return AdminCafeResponse.from(cafeRepository.save(cafe));
    }

    /**
     * 관리자 - 카페 정보 수정 (PATCH /api/V1/admin/cafe/{cafeId})
     * 전송된 필드만 수정, null인 필드는 기존값 유지
     */
    @Transactional
    public AdminCafeResponse updateCafe(Long cafeId, CafeUpdateRequest request) {
        // cafeId로 카페 조회, 존재하지 않으면 예외 발생
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        // type, franchise 둘 다 전송된 경우에만 일관성 검증
        if (request.getType() != null || request.getFranchise() != null) {
            CafeType type = request.getType() != null ? request.getType() : cafe.getType();
            Franchise franchise = request.getFranchise() != null ? request.getFranchise() : cafe.getFranchise();
            validateFranchiseConsistency(type, franchise);
        }

        // 더티체킹으로 UPDATE 실행 (@Transactional 범위 안에서 필드 변경 시 자동 반영)
        cafe.updateByAdmin(
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPhone(),
                request.getDescription(),
                request.getType(),
                request.getFranchise(),
                request.getHasToilet(),
                request.getHasOutlet(),
                request.getHasWifi(),
                request.getFloorCount(),
                request.getHasSeparateSpace(),
                request.getCongestionLevel(),
                request.getImageUrl()
        );

        return AdminCafeResponse.from(cafe);
    }

    // INDIVIDUAL 타입 ↔ Franchise 브랜드 일관성 검증
    private void validateFranchiseConsistency(CafeType type, Franchise franchise) {
        boolean hasBrand = franchise != null && franchise != Franchise.NONE;

        if (type == CafeType.INDIVIDUAL && hasBrand) {
            // 개인 카페인데 브랜드가 선택된 경우
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (type == CafeType.FRANCHISE && !hasBrand) {
            // 프랜차이즈인데 브랜드가 NONE이거나 없는 경우
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     관리자 - 카페 목록 조회 (페이징 및 필터링)
     **/
    @Transactional(readOnly = true)
    public PageResponse<AdminCafeResponse> getCafes(AdminCafeSearchCondition condition, int page) {
        // page는 클라이언트에서 1부터 들어오므로, JPA의 0-based index에 맞춰 1을 빼줌. 사이즈는 15.
        Pageable pageable = PageRequest.of(page - 1, 15); // 성능차 X

        // QueryDSL을 통해 조건에 맞는 Cafe Page 조회
        Page<Cafe> cafePage = cafeRepository.searchAdminCafes(condition, pageable);

        // Page<Cafe> 를 Page<AdminCafeResponse> 로 변환 후 PageResponse 객체로 매핑
        Page<AdminCafeResponse> dtoPage = cafePage.map(AdminCafeResponse::from);

        return PageResponse.of(dtoPage);
    }

    /**
     * 관리자 - 카페 상세 조회
     */
    @Transactional(readOnly = true)
    public AdminCafeResponse getCafe(Long cafeId) {
        // cafeId로 카페 조회, 존재하지 않으면 예외 발생
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        return AdminCafeResponse.from(cafe);
    }

    /**
     * 관리자 - 카페 정보 삭제 (DELETE /api/V1/admin/cafe/{cafeId})
     * 외래키 제약 조건으로 인해 연관 데이터(찜, 리뷰)를 먼저 삭제 후 카페 삭제
     */
    @Transactional
    public void deleteCafe(Long cafeId) {
        // cafeId로 카페 조회, 존재하지 않으면 예외 발생
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        // 1. 연관된 찜 목록 먼저 삭제
        wishlistRepository.deleteByCafeId(cafeId);

        // 2. 연관된 리뷰 먼저 삭제
        reviewRepository.deleteByCafeId(cafeId);

        // 3. 카페 삭제
        cafeRepository.delete(cafe);
    }

    /**
     * 관리자 - 사용자 카페 정보 등록 - 승인 (PATCH /api/V1/admin/cafe/{cafeId}/approve)
     * 이미 승인된 카페 재승인 시 409 에러
     */
    @Transactional
    public AdminCafeResponse approveCafe(Long cafeId) {
        // cafeId로 카페 조회, 존재하지 않으면 예외 발생
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        // 이미 승인된 카페 중복 처리 방지
        if (cafe.getStatus() == CafeStatus.APPROVED) {
            throw new CustomException(ErrorCode.CAFE_ALREADY_APPROVED);
        }

        // 더티체킹으로 UPDATE 실행
        cafe.approve();

        return AdminCafeResponse.from(cafe);
    }

    /**
     * 관리자 - 사용자 카페 정보 등록 - 거부 (PATCH /api/V1/admin/cafe/{cafeId}/reject)
     * 이미 거절된 카페 재거절 시 409 에러
     */
    @Transactional
    public AdminCafeResponse rejectCafe(Long cafeId) {
        // cafeId로 카페 조회, 존재하지 않으면 예외 발생
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        // 이미 거절된 카페 중복 처리 방지
        if (cafe.getStatus() == CafeStatus.REJECTED) {
            throw new CustomException(ErrorCode.CAFE_ALREADY_REJECTED);
        }

        // 더티체킹으로 UPDATE 실행
        cafe.reject();

        return AdminCafeResponse.from(cafe);
    }
}
