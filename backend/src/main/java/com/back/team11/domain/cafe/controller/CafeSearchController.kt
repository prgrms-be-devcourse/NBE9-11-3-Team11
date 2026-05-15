package com.back.team11.domain.cafe.controller

import com.back.team11.domain.cafe.dto.CafeDetailResponse
import com.back.team11.domain.cafe.dto.CafeListResponse
import com.back.team11.domain.cafe.repository.CafeSearchCondition
import com.back.team11.domain.cafe.service.CafeService
import com.back.team11.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Cafe", description = "카페 관련 API")
@RestController
@RequestMapping("/api/V1/cafe")
class CafeSearchController(
    private val cafeService: CafeService,
) {
    @Operation(summary = "카페 목록 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "카페 목록 조회 성공"),
    )
    @GetMapping
    fun searchCafes(
        @Valid @ModelAttribute condition: CafeSearchCondition,
    ): ResponseEntity<RsData<List<CafeListResponse>>> {
        val cafes = cafeService.searchCafes(condition)
        return ResponseEntity.ok(RsData("카페 목록 조회 성공", "200", cafes))
    }

    @Operation(summary = "카페 상세 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "카페 상세 조회 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 카페"),
    )
    @GetMapping("/{cafeId}")
    fun getCafe(
        @PathVariable cafeId: Long,
    ): ResponseEntity<RsData<CafeDetailResponse>> {
        val cafe = cafeService.getCafe(cafeId)
        return ResponseEntity.ok(RsData("카페 조회 성공", "200", cafe))
    }
}