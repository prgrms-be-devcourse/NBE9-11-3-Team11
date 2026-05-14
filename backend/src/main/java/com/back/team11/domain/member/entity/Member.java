package com.back.team11.domain.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(length = 100)
    private String providerId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Member create(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.email = email;
        member.password = passwordEncoder.encode(password); // 비밀번호 암호화
        member.nickname = nickname;
        member.role = MemberRole.USER;
        return member;
    }

    public static Member createOAuth(String email, String nickname, Provider provider, String providerId) {
        Member member = new Member();
        member.email = email;
        member.nickname = nickname;
        member.role = MemberRole.USER;
        member.provider = provider;
        member.providerId = providerId;
        return member;
    }

}

