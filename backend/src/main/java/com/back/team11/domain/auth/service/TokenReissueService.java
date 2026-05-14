package com.back.team11.domain.auth.service;

import com.back.team11.domain.auth.entity.RefreshToken;
import com.back.team11.domain.auth.repository.RefreshTokenRepository;
import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.global.util.CookieUtil;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.repository.MemberRepository;

import com.back.team11.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Access Token 재발급과 예외 처리
@Service
@RequiredArgsConstructor
public class TokenReissueService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;
    private final MemberRepository memberRepository; //추가


    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) {


        // Refresh Token은 HttpOnly 쿠키에서만 읽음
        // 쿠키에 없으면 재발급 요청 자체를 유효하지 않은 것으로 보기
        String refreshTokenValue = cookieUtil.getRefreshTokenFromCookie(request);

        if (refreshTokenValue == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // DB에서 RefreshToken 조회
        // 없으면 위조된 토큰으로 판단 → 예외 처리
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 만료 여부 확인
        if (refreshToken.isExpired()) {
            // 만료된 토큰은 DB에서 삭제
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }


        // 재발급 대상 사용자 식별
        Long memberId = refreshToken.getMemberId();

        //수정: MemberRepository에서 실제 role 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        String role = member.getRole().name();


        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);

        // DB에 새 RefreshToken 갱신 (rotate)
        LocalDateTime newExpiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000);
        refreshToken.rotate(newRefreshToken, newExpiresAt);

        // 새 토큰 쿠키에 담기 (CookieUtil 사용)
        cookieUtil.addAccessTokenCookie(response, newAccessToken);
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);
    }
}