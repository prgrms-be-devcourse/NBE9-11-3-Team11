package com.back.team11.domain.wishlist.service

import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.domain.wishlist.dto.WishlistResponse
import com.back.team11.domain.wishlist.entity.Wishlist
import com.back.team11.domain.wishlist.repository.WishlistRepository
import com.back.team11.global.dto.PageResponse
import com.back.team11.global.dto.toPageResponse
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.extension.findByIdOrThrow
import com.back.team11.global.util.AuthUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WishlistService(
    private val memberRepository: MemberRepository,
    private val cafeRepository: CafeRepository,
    private val wishlistRepository: WishlistRepository,
    private val authUtil: AuthUtil,
) {
    @Transactional
    fun addWishlist(cafeId: Long): WishlistResponse {
        // 멤버 임시 구현(JWT 도입 후 수정 예정)
        val member = memberRepository.findMemberById(
            authUtil.currentMemberId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        ) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        // 카페 존재 여부 확인
        val cafe = cafeRepository.findByIdOrThrow(cafeId) {
            CustomException(ErrorCode.CAFE_NOT_FOUND)
        }

        // Member는 한 카페에 한번만 찜 가능
        if (wishlistRepository.existsByMemberIdAndCafeId(member.id, cafeId)) {
            throw CustomException(ErrorCode.WISHLIST_ALREADY_EXISTS)
        }

        val wishlist = Wishlist.create(member, cafe)
        wishlistRepository.save(wishlist)

        return WishlistResponse.from(wishlist)
    }

    @Transactional
    fun deleteWishlist(cafeId: Long) {
        // 멤버 임시 구현(JWT 도입 후 수정 예정)
        val member = memberRepository.findMemberById(
            authUtil.currentMemberId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        ) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        // 카페 존재 여부 확인
        val cafe = cafeRepository.findByIdOrThrow(cafeId) {
            CustomException(ErrorCode.CAFE_NOT_FOUND)
        }

        // 찜 내역 없으면
        if (!wishlistRepository.existsByMemberIdAndCafeId(member.id, cafeId)) {
            throw CustomException(ErrorCode.WISHLIST_NOT_FOUND)
        }

        wishlistRepository.deleteByMemberIdAndCafeId(member.id, cafe.id)
    }

    fun getWishlists(pageable: Pageable): PageResponse<WishlistResponse> {
        val sortedPageable: Pageable = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val wishlists = wishlistRepository.findAllByMemberIdWithCafe(
            authUtil.currentMemberId ?: throw CustomException(ErrorCode.UNAUTHORIZED),
            sortedPageable
        )

        return wishlists.map { WishlistResponse.from(it) }.toPageResponse()
    }
}
