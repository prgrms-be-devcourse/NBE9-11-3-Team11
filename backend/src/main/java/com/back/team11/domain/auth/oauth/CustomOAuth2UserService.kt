package com.back.team11.domain.auth.oauth

import com.back.team11.domain.member.entity.Member
import com.back.team11.domain.member.repository.MemberRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = delegate.loadUser(userRequest)

        val attributes = OAuthAttributes.of(
            userRequest.clientRegistration.registrationId,
            oAuth2User.attributes
        )

        return findOrCreateMember(attributes).let { createPrincipal(it) }
    }

    private fun findOrCreateMember(attributes: OAuthAttributes): Member {
        memberRepository.findByProviderAndProviderId(attributes.provider, attributes.providerId)
            ?.let { return it }

        // 이미 다른 소셜 계정으로 가입된 이메일 체크
        memberRepository.findByEmail(attributes.email)?.let {
            throw OAuth2AuthenticationException(
                OAuth2Error("duplicate_email"),
                "이미 다른 소셜 계정으로 가입된 이메일입니다."
            )
        }

        return memberRepository.save(
            Member.createOAuth(
                email = attributes.email,
                nickname = attributes.nickname,
                provider = attributes.provider,
                providerId = attributes.providerId
            )
        )
    }

    private fun createPrincipal(member: Member): OAuth2User {
        val memberId = member.id
            ?: throw OAuth2AuthenticationException(
                OAuth2Error("invalid_member_id"),
                "회원 ID가 없습니다."
            )

        val providerName = member.provider?.name
            ?: throw OAuth2AuthenticationException(
                OAuth2Error("invalid_provider"),
                "OAuth provider 정보가 없습니다."
            )

        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_${member.role.name}")),
            mapOf(
                "memberId" to memberId,
                "email" to member.email,
                "nickname" to member.nickname,
                "provider" to providerName,
                "role" to member.role.name
            ),
            "memberId"
        )
    }
}