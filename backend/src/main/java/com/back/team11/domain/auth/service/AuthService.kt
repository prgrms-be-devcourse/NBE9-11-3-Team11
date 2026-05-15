package com.back.team11.domain.auth.service;

import com.back.team11.domain.auth.dto.LoginRequestDto;
import com.back.team11.domain.auth.entity.RefreshToken;
import com.back.team11.domain.auth.repository.RefreshTokenRepository;
import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.global.util.CookieUtil;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.MemberRole;
import com.back.team11.domain.member.service.MemberService;
import com.back.team11.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final CookieUtil cookieUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void adminLogin(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        Member member = memberService.findByEmail(loginRequestDto.getEmail());

        if (member == null ||
                !memberService.validatePassword(loginRequestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        // 관리자 로그인 컨트롤러이므로 ADMIN만 허용
        if (member.getRole() != MemberRole.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                member.getId(),
                member.getRole().name()
        );

        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(member.getId());

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(member.getId())
                .map(existingToken -> {
                    existingToken.rotate(refreshTokenValue, expiresAt);
                    return existingToken;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .memberId(member.getId())
                        .token(refreshTokenValue)
                        .expiresAt(expiresAt)
                        .build());

        refreshTokenRepository.save(refreshToken);

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshTokenValue);
    }


    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refreshToken 꺼내기
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

        // DB에서 RefreshToken 삭제
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }

        // 쿠키 삭제
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
    }
}
