package com.back.team11.domain.cafe.controller

import com.back.team11.domain.cafe.dto.CafeRequest
import com.back.team11.domain.cafe.dto.CafeResponse
import com.back.team11.domain.cafe.service.CafeService
import com.back.team11.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Cafe", description = "카페 관련 API")
@RestController
@RequestMapping("/api/V1/cafe")
class CafeController(
    private val cafeService: CafeService,
) {
    /**
     * 사용자 - 카페 정보 생성 요청(제보) (POST /api/V1/cafe/report)
     * JWT 쿠키에서 로그인한 사용자 ID를 자동으로 추출하여 제보자로 연결
     */
    @Operation(summary = "카페 제보")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "카페 제보 접수 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 입력값"),
        ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
    )
    @PostMapping("/report")
    fun reportCafe(
        @AuthenticationPrincipal memberId: Long,
        @RequestBody @Valid request: CafeRequest,
    ): ResponseEntity<RsData<CafeResponse>> {
        val response = cafeService.reportCafe(memberId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData("카페 제보가 접수되었습니다.", "201", response))
    }
}
