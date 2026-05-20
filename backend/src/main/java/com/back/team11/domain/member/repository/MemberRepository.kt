package com.back.team11.domain.member.repository

import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.entity.Provider
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByProviderAndProviderId(provider: Provider, providerId: String): Member?

    fun findByEmail(email: String): Member?

    fun findMemberById(id: Long): Member?

    fun existsByEmail(email: String): Boolean
}
