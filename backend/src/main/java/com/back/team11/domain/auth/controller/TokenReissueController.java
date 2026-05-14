package com.back.team11.domain.auth.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Refresh Token 기반으로 Access Token을 재발급하는 엔드포인트
// 실제 검증 및 재발급 로직은 Service 계층에서 처리

// 쿠키 관련 처리는 CookieUtil, 재발급 로직은 Service에서 수행

@Tag(name = "Token Reissue", description = "Access Token 재발급 API")
@RestController
@RequestMapping("/api/V1/auth")
@RequiredArgsConstructor
public class TokenReissueController {

    private final TokenReissueService tokenReissueService;


    //Access Token 재발급 API
    @PostMapping("/refresh")
    @Operation(summary = "리프레쉬 토큰 재발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레쉬 토큰"),
            @ApiResponse(responseCode = "401", description = "만료된 리프레쉬 토큰")
    })
    public ResponseEntity<RsData<Void>> refresh(HttpServletRequest request,
                                                HttpServletResponse response) {
        tokenReissueService.reissue(request, response);
        return ResponseEntity.ok(new RsData<>("토큰 재발급 성공", "200"));
    }
}