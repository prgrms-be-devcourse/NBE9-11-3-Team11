package com.back.team11.domain.review.controller

import com.back.team11.domain.review.dto.ReviewRequestDto
import com.back.team11.domain.review.dto.ReviewResponseDto
import com.back.team11.domain.review.service.ReviewService
import com.back.team11.global.dto.PageResponse
import com.back.team11.global.rsData.RsData
import com.back.team11.global.util.AuthUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/V1/cafe/{cafeId}/reviews")
class ReviewController (
    private val reviewService: ReviewService,
    private val authUtil: AuthUtil
){

    //리뷰 작성
    @PostMapping
    @Operation(summary = "리뷰 생성")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "리뷰 생성 성공"),
        ApiResponse(responseCode = "409", description = "이미 리뷰를 작성한 카페")
    )
    fun createReview(
        @PathVariable cafeId: Long,
        @RequestBody @Valid requestDto: ReviewRequestDto
    ): ResponseEntity<RsData<ReviewResponseDto?>> {
        val memberId = authUtil.currentMemberId
        val data = reviewService.createReview(cafeId, requestDto, memberId)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body<RsData<ReviewResponseDto?>>(RsData<ReviewResponseDto?>("리뷰가 작성되었습니다.", "201", data))
    }

    //리뷰조회
    @GetMapping
    @Operation(summary = "리뷰 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "리뷰 생성 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 카페")
    )
    fun getReviews(
        @PathVariable cafeId: Long
    ): ResponseEntity<RsData<List<ReviewResponseDto?>?>> {
        val data = reviewService.getReviews(cafeId)
        return ResponseEntity.ok<RsData<List<ReviewResponseDto?>?>>(
            RsData<List<ReviewResponseDto?>?>(
                "리뷰 목록 조회 성공",
                "200",
                data
            )
        )
    }

    //페이징 리뷰조회
    @GetMapping("/page")
    @Operation(summary = "페이징 리뷰 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "페이징 리뷰 조회 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 카페")
    )
    fun getReviewsPage(
        @PathVariable cafeId: Long,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<RsData<PageResponse<ReviewResponseDto?>?>> {
        val data = reviewService.getReviewsPage(cafeId, pageable)
        return ResponseEntity.ok<RsData<PageResponse<ReviewResponseDto?>?>>(
            RsData<PageResponse<ReviewResponseDto?>?>(
                "리뷰 페이징 조회 성공",
                "200",
                data
            )
        )
    }

    //리뷰수정
    @PutMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
        ApiResponse(responseCode = "403", description = "리뷰 작성자만 수정/삭제 가능"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 리뷰")
    )
    fun updateReview(
        @PathVariable cafeId: Long,
        @PathVariable reviewId: Long,
        @RequestBody @Valid requestDto: ReviewRequestDto
    ): ResponseEntity<RsData<ReviewResponseDto?>> {
        val memberId = authUtil.currentMemberId
        val data = reviewService.updateReview(cafeId, reviewId, requestDto, memberId)
        return ResponseEntity.ok<RsData<ReviewResponseDto?>>(RsData<ReviewResponseDto?>("리뷰가 수정되었습니다.", "200", data))
    }

    //리뷰삭제
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
        ApiResponse(responseCode = "403", description = "리뷰 작성자만 수정/삭제 가능"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 리뷰")
    )
    fun deleteReview(
        @PathVariable cafeId: Long,
        @PathVariable reviewId: Long
    ): ResponseEntity<RsData<Void?>> {
        val memberId = authUtil.currentMemberId
        reviewService.deleteReview(cafeId, reviewId, memberId)
        return ResponseEntity.ok<RsData<Void?>>(RsData<Void?>("리뷰가 삭제되었습니다.", "200"))
    }
}
