package com.back.team11

import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.MemberRole
import com.back.team11.domain.member.repository.MemberRepository
import com.back.team11.domain.member.service.MemberService
import lombok.RequiredArgsConstructor
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val memberService: MemberService,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String) {
        // 암호화된 비밀번호 생성
        val email = "admin@test.com"
        val password = "1234" // 초기 비밀번호
        val encodedPassword = passwordEncoder.encode(password) // BCrypt로 암호화

        // 회원이 없으면 관리자 계정 생성
        if (memberService.findByEmail(email) == null) {
            memberService.createMember(email, encodedPassword!!, "관리자")
            println("Admin account created successfully.")
        } else {
            // 이미 관리자가 있으면 암호화된 비밀번호를 갱신
            memberService.updatePasswordByEmail(email, encodedPassword!!) // 비밀번호 갱신
            println("Admin account already exists, password updated.")
        }

        // ADMIN role 보장
        val admin = memberService.findByEmail(email)
        admin!!.role = MemberRole.ADMIN
        memberRepository.save(admin)
    }
}