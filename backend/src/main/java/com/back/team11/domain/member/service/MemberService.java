package com.back.team11.domain.member.service;

import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import com.back.team11.domain.member.dto.MemberResponseDto;
import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder; // 비밀번호 암호화 및 비교용

    // 이메일로 사용자 조회
    public Member findByEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.orElse(null);  // 사용자가 없을 경우 예외 처리
    }

    // 회원 생성 시 비밀번호 암호화하여 저장
    public void createMember(String email, String password, String nickname) {
        Member member = Member.create(email, password, nickname, passwordEncoder); // 비밀번호 암호화
        memberRepository.save(member); // 암호화된 비밀번호로 회원 저장
    }

    // 비밀번호 갱신 (관리자 계정 비밀번호 업데이트용)
    public void updatePasswordByEmail(String email, String encodedPassword) {
        Member member = findByEmail(email);
        if (member != null) {
            member.setPassword(encodedPassword); // 비밀번호 갱신
            memberRepository.save(member); // DB에 저장
        }
    }

    // 비밀번호 검증
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        System.out.println("비밀번호 검증 시작: " + rawPassword + " / " + encodedPassword);
        boolean isValid = passwordEncoder.matches(rawPassword, encodedPassword);
        if (isValid) {
            System.out.println("비밀번호 일치: " + rawPassword + " / " + encodedPassword);
        } else {
            System.out.println("비밀번호 불일치: " + rawPassword + " / " + encodedPassword);
        }
        return isValid;
    }

    public MemberResponseDto getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponseDto.from(member);
    }
}