package com.back.team11.domain.member.service

import com.back.team11.domain.member.dto.MemberResponseDto
import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    @Lazy private val passwordEncoder: PasswordEncoder // 비밀번호 암호화 및 비교용
) {
    // 이메일로 사용자 조회
    fun findByEmail(email: String): Member? {
        return memberRepository.findByEmail(email).orElse(null) // 사용자가 없을 경우 null 반환
    }

    // 회원 생성 시 비밀번호 암호화하여 저장
    fun createMember(email: String, password: String, nickname: String) {
        val member = Member.create(email, password, nickname, passwordEncoder) // 비밀번호 암호화
        memberRepository.save(member) // 암호화된 비밀번호로 회원 저장
    }

    // 비밀번호 갱신 (관리자 계정 비밀번호 업데이트용)
    fun updatePasswordByEmail(email: String, encodedPassword: String) {
        val member = findByEmail(email)
        if (member != null) {
            member.password = encodedPassword // 비밀번호 갱신
            memberRepository.save(member) // DB에 저장
        }
    }

    // 비밀번호 검증
    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean {
        println("비밀번호 검증 시작: $rawPassword / $encodedPassword")
        val isValid = passwordEncoder.matches(rawPassword, encodedPassword)
        if (isValid) {
            println("비밀번호 일치: $rawPassword / $encodedPassword")
        } else {
            println("비밀번호 불일치: $rawPassword / $encodedPassword")
        }
        return isValid
    }

    fun getMe(memberId: Long): MemberResponseDto {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }
        return MemberResponseDto.from(member)
    }
}