package com.back.team11.domain.member.repository

import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.Provider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByProviderAndProviderId(provider: Provider, providerId: String): Optional<Member>

    fun findByEmail(email: String): Optional<Member>

    fun existsByEmail(email: String): Boolean
}
