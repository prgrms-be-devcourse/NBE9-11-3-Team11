package com.back.team11.domain.wishlist.service;

import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.global.dto.PageResponse;
import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.global.util.AuthUtil;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.repository.MemberRepository;
import com.back.team11.domain.wishlist.dto.WishlistResponse;
import com.back.team11.domain.wishlist.entity.Wishlist;
import com.back.team11.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishlistService {

    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;
    private final WishlistRepository wishlistRepository;
    private final AuthUtil authUtil;


    @Transactional
    public WishlistResponse addWishlist(Long cafeId) {

        // 멤버 임시 구현(JWT 도입 후 수정 예정)
        Member member = memberRepository.findById(authUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 카페 존재 여부 확인
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        // Member는 한 카페에 한번만 찜 가능
        if(wishlistRepository.existsByMemberIdAndCafeId(member.getId(), cafeId)){
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Wishlist wishlist = Wishlist.create(member, cafe);

        wishlistRepository.save(wishlist);

        return WishlistResponse.from(wishlist);
    }

    @Transactional
    public void deleteWishlist(Long cafeId) {
        // 멤버 임시 구현(JWT 도입 후 수정 예정)
        Member member = memberRepository.findById(authUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 카페 존재 여부 확인
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        // 찜 내역 없으면
        if(!wishlistRepository.existsByMemberIdAndCafeId(member.getId(), cafeId)){
            throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
        }

        wishlistRepository.deleteByMemberIdAndCafeId(member.getId(), cafe.getId());
    }

    public PageResponse<WishlistResponse> getWishlists(Pageable pageable) {

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Wishlist> wishlists = wishlistRepository.findAllByMemberIdWithCafe(
                authUtil.getCurrentMemberId(), sortedPageable
        );

        return PageResponse.from(wishlists.map(WishlistResponse::from));
    }
}
