package com.back.team11.domain.member.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    var password: String? = null,

    @Column(nullable = false, length = 50)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    var role: MemberRole = MemberRole.USER,

    @Enumerated(EnumType.STRING)
    var provider: Provider? = null,

    @Column(length = 100)
    var providerId: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null,
) {
    companion object {
        @JvmStatic
        fun create(
            email: String,
            password: String,
            nickname: String,
            passwordEncoder: PasswordEncoder
        ): Member {
            return Member(
                email = email,
                password = passwordEncoder.encode(password), // 비밀번호 암호화
                nickname = nickname,
                role = MemberRole.USER
            )
        }

        @JvmStatic
        fun createOAuth(
            email: String,
            nickname: String,
            provider: Provider,
            providerId: String
        ): Member {
            return Member(
                email = email,
                nickname = nickname,
                role = MemberRole.USER,
                provider = provider,
                providerId = providerId
            )
        }
    }
}
