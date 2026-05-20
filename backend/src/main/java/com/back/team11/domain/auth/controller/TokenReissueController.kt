package com.back.team11.domain.auth.controller

import com.back.team11.domain.auth.service.TokenReissueService
import com.back.team11.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Token Reissue", description = "Access Token 재발급 API")
@RestController
@RequestMapping("/api/V1/auth")
class TokenReissueController(
    private val tokenReissueService: TokenReissueService
) {

    @PostMapping("/refresh")
    @Operation(summary = "리프레쉬 토큰 재발급")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
        ApiResponse(responseCode = "401", description = "유효하지 않은 리프레쉬 토큰"),
        ApiResponse(responseCode = "401", description = "만료된 리프레쉬 토큰")
    )
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<RsData<Unit>> =
        tokenReissueService.reissue(request, response)
            .let { ResponseEntity.ok(RsData("토큰 재발급 성공", "200")) }
}