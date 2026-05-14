package com.back.team11;

import com.back.team11.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 암호화된 비밀번호 생성
        String email = "admin@test.com";
        String password = "1234"; // 초기 비밀번호
        String encodedPassword = passwordEncoder.encode(password); // BCrypt로 암호화

        // 회원이 없으면 관리자 계정 생성
        if (memberService.findByEmail(email) == null) {
            memberService.createMember(email, encodedPassword, "관리자");
            System.out.println("Admin account created successfully.");
        } else {
            // 이미 관리자가 있으면 암호화된 비밀번호를 갱신
            memberService.updatePasswordByEmail(email, encodedPassword); // 비밀번호 갱신
            System.out.println("Admin account already exists, password updated.");
        }
    }
}