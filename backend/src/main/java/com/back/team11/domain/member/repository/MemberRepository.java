package com.back.team11.domain.member.repository;

import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
