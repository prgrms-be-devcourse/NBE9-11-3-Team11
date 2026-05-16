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
    fun findByEmail(email: String): Member? =
        memberRepository.findByEmail(email).orElse(null)

    // 회원 생성 시 비밀번호 암호화하여 저장
    fun createMember(email: String, password: String, nickname: String) {
        val member = Member.create(email, password, nickname, passwordEncoder)
        memberRepository.save(member)
    }

    // 비밀번호 갱신 (관리자 계정 비밀번호 업데이트용)
    fun updatePasswordByEmail(email: String, encodedPassword: String) {
        findByEmail(email)?.let { member ->
            member.password = encodedPassword
            memberRepository.save(member)
        }
    }

    // 비밀번호 검증
    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)

    fun getMe(memberId: Long): MemberResponseDto =
        memberRepository.findById(memberId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }
            .let { MemberResponseDto.from(it) }
}