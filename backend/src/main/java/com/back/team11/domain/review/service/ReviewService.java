package com.back.team11.domain.review.service;


import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.cafe.repository.CafeRepository;
import com.back.team11.global.dto.PageResponse;
import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.global.util.AuthUtil;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.repository.MemberRepository;
import com.back.team11.domain.review.dto.ReviewRequestDto;
import com.back.team11.domain.review.dto.ReviewResponseDto;
import com.back.team11.domain.review.entity.Review;
import com.back.team11.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final CafeRepository cafeRepository;
    private final MemberRepository memberRepository;
    private final AuthUtil authUtil;

    // 리뷰 작성
    public ReviewResponseDto createReview(Long cafeId, ReviewRequestDto requestDto, Long memberId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (reviewRepository.existsByMemberIdAndCafeId(memberId, cafeId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review(member, cafe, requestDto.getContent());
        reviewRepository.save(review);

        return ReviewResponseDto.from(review);
    }

    //리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviews(Long cafeId) {
        cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        return reviewRepository.findAllByCafeIdWithFetch(cafeId).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    //페이징 리뷰 조회
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponseDto> getReviewsPage(Long cafeId, Pageable pageable) {
        cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFE_NOT_FOUND));

        //정렬 (createdAt desc)
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );


        Page<ReviewResponseDto> page = reviewRepository
                .findAllByCafeIdWithFetch(cafeId, sortedPageable)
                .map(ReviewResponseDto::from);

        return PageResponse.from(page);
    }

    //리뷰 수정
    public ReviewResponseDto updateReview(Long cafeId, Long reviewId, ReviewRequestDto requestDto, Long memberId) {
        Review review = reviewRepository.findByIdAndCafeIdWithFetch(reviewId, cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_REVIEW);
        }

        review.update(requestDto.getContent());

        return ReviewResponseDto.from(review);
    }

    //리뷰 삭제
    public void deleteReview(Long cafeId, Long reviewId, Long memberId) {
        Review review = reviewRepository.findByIdAndCafeId(reviewId, cafeId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인이 아니고, 관리자도 아니면 예외
        if (!review.getMember().getId().equals(memberId) && !authUtil.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN_REVIEW);
        }

        reviewRepository.delete(review);
    }
}
