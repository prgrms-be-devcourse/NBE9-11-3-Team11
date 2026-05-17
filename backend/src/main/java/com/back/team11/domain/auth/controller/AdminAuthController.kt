package com.back.team11.domain.auth.controller

import com.back.team11.domain.auth.dto.LoginRequestDto
import com.back.team11.domain.auth.service.AuthService
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Auth", description = "관리자 인증 관련 API")
@RestController
@RequestMapping("/api/V1/admin/auth")
class AdminAuthController(
    private val tokenReissueService: TokenReissueService,
    private val authService: AuthService
) {

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인", security = [])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    )
    fun login(
        @RequestBody loginRequestDto: LoginRequestDto,
        response: HttpServletResponse
    ): ResponseEntity<RsData<Unit>> =
        authService.adminLogin(loginRequestDto, response)
            .let { ResponseEntity.ok(RsData("관리자 로그인이 성공적으로 되었습니다.", "200")) }

    @PostMapping("/refresh")
    @Operation(summary = "관리자 리프레쉬 토큰 발급")
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
            .let { ResponseEntity.ok(RsData("토큰 재발급이 성공적으로 되었습니다.", "200")) }

    @PostMapping("/logout")
    @Operation(summary = "관리자 로그아웃")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요.")
    )
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<RsData<Unit>> =
        authService.logout(request, response)
            .let { ResponseEntity.ok(RsData("로그아웃이 성공적으로 되었습니다.", "200")) }
}