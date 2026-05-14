package com.back.team11.domain.auth.controller;

import com.back.team11.domain.auth.dto.LoginRequestDto;
import com.back.team11.domain.auth.service.AuthService;
import com.back.team11.domain.auth.service.TokenReissueService;
import com.back.team11.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Auth", description = "관리자 인증 관련 API")
@RestController
@RequestMapping("/api/V1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final TokenReissueService tokenReissueService;
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인",
            security={}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    public ResponseEntity<RsData<Void>> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ) {
        authService.adminLogin(loginRequestDto, response);

        return ResponseEntity.ok(
                new RsData<>(
                        "관리자 로그인이 성공적으로 되었습니다.",
                        "200",
                        null
                )
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "관리자 리프레쉬 토큰 발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레쉬 토큰"),
            @ApiResponse(responseCode = "401", description = "만료된 리프레쉬 토큰")
    })
    public ResponseEntity<RsData<Void>> refresh(HttpServletRequest request,
                                                HttpServletResponse response) {

        tokenReissueService.reissue(request, response);

        // 리프레시 토큰이 없을 경우
        return ResponseEntity.ok(new RsData<>("토큰 재발급이 성공적으로 되었습니다.","200"));
    }

    @PostMapping("/logout")
    @Operation(summary = "관리자 로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
    })
    public ResponseEntity<RsData<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);

        return ResponseEntity.ok(
                new RsData<>(
                        "로그아웃이 성공적으로 되었습니다.",
                        "200",
                        null
                )
        );
    }
}