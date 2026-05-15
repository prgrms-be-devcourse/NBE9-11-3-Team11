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

        val registrationId = userRequest.clientRegistration.registrationId

        val attributes = OAuthAttributes.of(
            registrationId,
            oAuth2User.attributes
        )

        validateAttributes(attributes)

        val member = findOrCreateMember(attributes)

        return createPrincipal(member)
    }

    private fun validateAttributes(attributes: OAuthAttributes) {
        if (attributes.providerId.isNullOrBlank()) {
            throw OAuth2AuthenticationException(
                OAuth2Error("invalid_provider_id"),
                "OAuth providerId가 없습니다."
            )
        }
    }

    private fun findOrCreateMember(attributes: OAuthAttributes): Member {
        val providerId = attributes.providerId
            ?: throw OAuth2AuthenticationException(
                OAuth2Error("invalid_provider_id"),
                "OAuth providerId가 없습니다."
            )

        val existingMember = memberRepository.findByProviderAndProviderId(
            attributes.provider,
            providerId
        )

        if (existingMember.isPresent) {
            return existingMember.get()
        }

        if (!attributes.email.isNullOrBlank()) {
            memberRepository.findByEmail(attributes.email)
                .ifPresent {
                    throw OAuth2AuthenticationException(
                        OAuth2Error("duplicate_email"),
                        "이미 다른 소셜 계정으로 가입된 이메일입니다."
                    )
                }
        }

        val nickname = if (attributes.nickname.isNullOrBlank()) {
            "${attributes.provider.name.lowercase()}_$providerId"
        } else {
            attributes.nickname
        }

        val email = if (attributes.email.isNullOrBlank()) {
            "${attributes.provider.name.lowercase()}_$providerId@oauth.local"
        } else {
            attributes.email
        }

        val newMember = Member.createOAuth(
            email = email,
            nickname = nickname,
            provider = attributes.provider,
            providerId = providerId
        )

        return memberRepository.save(newMember)
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

        val customAttributes = mapOf<String, Any>(
            "memberId" to memberId,
            "email" to member.email,
            "nickname" to member.nickname,
            "provider" to providerName,
            "role" to member.role.name
        )

        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_${member.role.name}")),
            customAttributes,
            "memberId"
        )
    }
}