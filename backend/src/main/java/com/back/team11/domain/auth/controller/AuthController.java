package com.back.team11.domain.auth.controller;


import com.back.team11.domain.auth.service.AuthService;
import com.back.team11.global.rsData.RsData;
import com.back.team11.global.util.AuthUtil;
import com.back.team11.domain.member.dto.MemberResponseDto;
import com.back.team11.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Auth", description = "사용자 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/V1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthUtil authUtil;
    private final MemberService memberService;

    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요.")
    })
    public ResponseEntity<RsData<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.ok(new RsData<>("로그아웃 성공", "200"));
    }

    @GetMapping("/me")
    @Operation(summary = "사용자 내 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 내 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용해 주세요.")
    })
    public ResponseEntity<RsData<MemberResponseDto>> getMe() {
        Long memberId = authUtil.getCurrentMemberId();
        return ResponseEntity.ok(new RsData<>("내 정보 조회 성공", "200", memberService.getMe(memberId)));
    }
}
