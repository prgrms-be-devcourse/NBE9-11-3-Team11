package com.back.team11.domain.review.service

import com.back.team11.domain.cafe.entity.QCafe.cafe
import com.back.team11.domain.cafe.repository.CafeRepository
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.domain.review.dto.ReviewRequestDto
import com.back.team11.domain.review.dto.ReviewResponseDto
import com.back.team11.domain.review.dto.ReviewResponseDto.Companion.from
import com.back.team11.domain.review.entity.Review
import com.back.team11.domain.review.repository.ReviewRepository
import com.back.team11.global.dto.PageResponse
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import com.back.team11.global.extension.findByIdOrThrow
import com.back.team11.global.util.AuthUtil
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors


@Service
@Transactional
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val cafeRepository: CafeRepository,
    private val memberRepository: MemberRepository,
    private val authUtil: AuthUtil
) {

    // 리뷰 작성
    fun createReview(cafeId: Long, requestDto: ReviewRequestDto, memberId: Long?): ReviewResponseDto {
        val resolvedMemberId = memberId ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        val cafe = cafeRepository.findByIdOrThrow(cafeId) {CustomException(ErrorCode.CAFE_NOT_FOUND)}

        val member = memberRepository.findMemberById(resolvedMemberId) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        if (reviewRepository.existsByMemberIdAndCafeId(resolvedMemberId, cafeId)) {
            throw CustomException(ErrorCode.REVIEW_ALREADY_EXISTS)
        }

        val review = Review(
            member = member,
            cafe = cafe,
            content = requestDto.content
        )

        return ReviewResponseDto.from(reviewRepository.save(review))
    }

    // 리뷰 조회
    @Transactional(readOnly = true)
    fun getReviews(cafeId: Long): List<ReviewResponseDto> {
        cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }

        return reviewRepository.findAllByCafeIdWithFetch(cafeId)
            .map { ReviewResponseDto.from(it) }
    }

    // 페이징 리뷰 조회
    @Transactional(readOnly = true)
    fun getReviewsPage(cafeId: Long, pageable: Pageable): PageResponse<ReviewResponseDto> {
        cafeRepository.findByIdOrThrow(cafeId) { CustomException(ErrorCode.CAFE_NOT_FOUND) }

        val sortedPageable = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        return reviewRepository
            .findAllByCafeIdWithFetch(cafeId, sortedPageable)
            .map { ReviewResponseDto.from(it) }
            .let { PageResponse.from(it) }
    }

    // 리뷰 수정
    fun updateReview(cafeId: Long, reviewId: Long, requestDto: ReviewRequestDto, memberId: Long?): ReviewResponseDto {
        val resolvedMemberId = memberId ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        val review = reviewRepository.findByIdAndCafeIdWithFetch(reviewId, cafeId)
            ?: throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        if (review.member.id != resolvedMemberId) {
            throw CustomException(ErrorCode.FORBIDDEN_REVIEW)
        }

        review.update(requestDto.content)

        return ReviewResponseDto.from(review)
    }

    // 리뷰 삭제
    fun deleteReview(cafeId: Long, reviewId: Long, memberId: Long?) {
        val resolvedMemberId = memberId ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        val review = reviewRepository.findByIdAndCafeId(reviewId, cafeId)
            ?: throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        if (review.member.id != resolvedMemberId && !authUtil.isAdmin) {
            throw CustomException(ErrorCode.FORBIDDEN_REVIEW)
        }

        reviewRepository.delete(review)
    }
}
