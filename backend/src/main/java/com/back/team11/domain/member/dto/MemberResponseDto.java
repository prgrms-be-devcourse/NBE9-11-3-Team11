package com.back.team11.domain.member.dto;

import com.back.team11.domain.member.entity.Member;

public record MemberResponseDto(
        Long memberId,
        String nickname,
        String email,
        String role
) {
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getNickname(),
                member.getEmail(),
                member.getRole().name()
        );
    }
}
