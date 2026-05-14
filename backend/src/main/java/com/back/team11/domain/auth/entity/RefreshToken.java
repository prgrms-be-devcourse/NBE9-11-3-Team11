package com.back.team11.domain.auth.entity;

import com.back.team11.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member 객체 대신 memberId만 저장하도록 수정
    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // RefreshToken 생성용 생성자
    // @Builder로 간편하게 객체 생성할 때 사용
    @Builder
    public RefreshToken(Long memberId, String token, LocalDateTime expiresAt) {
        this.memberId = memberId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    //만료 확인 메서드
    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 새로운 토큰으로 변경
    public void rotate(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }
}

