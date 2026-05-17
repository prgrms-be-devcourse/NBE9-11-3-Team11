package com.back.team11.domain.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_token")
class RefreshToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false, unique = true)
    val memberId: Long,

    @Column(nullable = false, length = 500)
    var token: String,

    @Column(nullable = false)
    var expiresAt: LocalDateTime

) {
    val isExpired: Boolean
        get() = LocalDateTime.now() > expiresAt

    fun rotate(newToken: String, newExpiresAt: LocalDateTime) = also {
        token = newToken
        expiresAt = newExpiresAt
    }
}