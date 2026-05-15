package com.back.team11.domain.wishlist.controller

import com.back.team11.domain.wishlist.dto.WishlistResponse
import com.back.team11.domain.wishlist.service.WishlistService
import com.back.team11.global.dto.PageResponse
import com.back.team11.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Wishlist", description = "찜 관련 API")
@RestController
@RequestMapping("/api/V1")
class WishlistController(
    private val wishlistService: WishlistService,
) {
    // 찜 추가
    @Operation(summary = "찜 추가")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "찜 추가 성공"),
        ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
        ApiResponse(responseCode = "404", description = "존재하지 않는 카페"),
        ApiResponse(responseCode = "409", description = "이미 찜한 카페")
    )
    @PostMapping("/cafe/{cafeId}/wishlist")
    fun addWishList(@PathVariable cafeId: Long): ResponseEntity<RsData<WishlistResponse>> {
        val wishlist = wishlistService.addWishlist(cafeId)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(RsData("찜이 추가되었습니다.", "201", wishlist))
    }

    // 찜 취소
    @Operation(summary = "찜 취소")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "찜 취소 성공"),
        ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
        ApiResponse(responseCode = "404", description = "존재하지 않는 찜 내역")
    )
    @DeleteMapping("/cafe/{cafeId}/wishlist")
    fun deleteWishlist(@PathVariable cafeId: Long): ResponseEntity<RsData<Void>> {
        wishlistService.deleteWishlist(cafeId)
        return ResponseEntity.ok(RsData("찜이 취소되었습니다.", "200"))
    }

    // 내 찜 목록 조회, 필요시 페이지 기능 추가
    @Operation(summary = "내 찜 목록 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "찜 목록 조회 성공"),
        ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요.")
    )
    @GetMapping("/member/me/wishlist")
    fun getWishlists(
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<RsData<PageResponse<WishlistResponse>>> {
        val wishlists = wishlistService.getWishlists(pageable)
        return ResponseEntity.ok(RsData("찜 목록 조회 성공", "200", wishlists))
    }
}
