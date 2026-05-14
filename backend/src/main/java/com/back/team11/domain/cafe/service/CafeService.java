package com.back.team11.domain.cafe.service;

import com.back.team11.domain.cafe.dto.CafeDetailResponse;
import com.back.team11.domain.cafe.dto.CafeListResponse;
import com.back.team11.domain.cafe.dto.CafeRequest;
import com.back.team11.domain.cafe.dto.CafeResponse;
import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.domain.cafe.repository.CafeSearchCondition;
import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.global.util.AuthUtil;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.repository.MemberRepository;
import com.back.team11.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeService {
    private final CafeRepository cafeRepository;
    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final AuthUtil authUtil;

    // 카페 목록 조회(지역 검색 + 필터링)
    public List<CafeListResponse> searchCafes(CafeSearchCondition condition) {
        List<Cafe> cafes = cafeRepository.searchCafes(condition);
        return cafes.stream()
                .map(cafe -> {
                    long wishlistCount = wishlistRepository.countByCafeId(cafe.getId());
                    return CafeListResponse.from(cafe, wishlistCount);
                })
                .toList();
    }

    // 카페 상세보기
    public CafeDetailResponse getCafe(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));
        long wishlistCount = wishlistRepository.countByCafeId(cafeId);
        Long memberId = authUtil.getCurrentMemberIdOrNull();
        boolean isWishlisted = memberId != null &&
                wishlistRepository.existsByMemberIdAndCafeId(memberId, cafeId);

        return CafeDetailResponse.from(cafe, wishlistCount, isWishlisted);
    }

    /**
     * 사용자 - 카페 정보 생성 요청(제보) (POST /api/V1/cafe/report)
     * 로그인한 사용자 정보를 member 필드에 연결, status는 PENDING으로 저장
     */
    @Transactional
    public CafeResponse reportCafe(Long memberId, CafeRequest request) {
        // 로그인한 사용자 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Cafe cafe = Cafe.createByUser(
                member,
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

        return CafeResponse.from(cafeRepository.save(cafe));
    }
}
