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
    val id: Long =0,

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
        fun create(
            email: String,
            password: String,
            nickname: String,
            passwordEncoder: PasswordEncoder
        ): Member = Member(
            email = email,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            role = MemberRole.USER
        )

        fun createOAuth(
            email: String?,
            nickname: String,
            provider: Provider,
            providerId: String
        ): Member = Member(
            email =  email ?: "",
            nickname = nickname,
            role = MemberRole.USER,
            provider = provider,
            providerId = providerId
        )
    }
}
