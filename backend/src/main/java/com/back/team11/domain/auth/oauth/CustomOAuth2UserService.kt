package com.back.team11.domain.auth.oauth;

import com.back.team11.domain.member.entity.Member;
import com.back.team11.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // 회원 조회/저장을 위한 Repository
    private final MemberRepository memberRepository;

    // Spring Security가 기본 제공하는 OAuth2 사용자 정보 조회 서비스
    // 실제로 카카오/구글/네이버 등에 접근해서 사용자 정보를 가져오는 역할
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1) OAuth 제공자(카카오 등)에서 사용자 원본 정보 받아오기
        //    예: 카카오에서 id, email, nickname 등의 정보를 응답으로 받음
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2) 현재 로그인한 OAuth 제공자가 누구인지 확인
        //    예: "kakao", "naver", "google"
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3) 제공자별로 다른 응답 구조를 공통 형식으로 변환
        //    예: 카카오는 kakao_account 안에 정보가 있고,
        //        구글은 바로 email, name 등이 있을 수 있으므로
        //        이를 OAuthAttributes라는 공통 DTO로 맞춰줌
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        // 4) 로그인 처리에 필요한 필수값 검증
        //    providerId 같은 값이 없으면 정상적인 OAuth 회원으로 처리할 수 없음
        validateAttributes(attributes);

        // 5) 기존 회원인지 조회하고,
        //    없으면 자동 회원가입 처리 후 Member 엔티티 반환
        Member member = findOrCreateMember(attributes);

        // 6) Spring Security가 인증된 사용자로 사용할 Principal 객체 생성 후 반환
        return createPrincipal(member);
    }

    /**
     * OAuth 응답값 중 필수값 검증
     */
    private void validateAttributes(OAuthAttributes attributes) {
        // providerId는 각 OAuth 제공자에서 사용자를 구분하는 고유 ID
        // 이 값이 없으면 어떤 사용자와 연결해야 하는지 알 수 없음
        if (attributes.getProviderId() == null || attributes.getProviderId().isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_provider_id"),
                    "OAuth providerId가 없습니다."
            );
        }

        // 이메일을 필수로 받을 정책이라면 아래 주석을 해제해서 사용 가능
        // 현재는 카카오 등에서 이메일 동의를 안 한 경우도 처리할 수 있도록 막아둔 상태
//        if (attributes.getEmail() == null || attributes.getEmail().isBlank()) {
//            throw new OAuth2AuthenticationException(
//                    new OAuth2Error("invalid_email"),
//                    "이메일 제공에 동의하지 않은 사용자입니다."
//            );
//        }
    }

    /**
     * 기존 회원 조회 또는 신규 회원 자동 생성
     */
    private Member findOrCreateMember(OAuthAttributes attributes) {
        // 1. provider + providerId 조합으로 이미 가입된 회원인지 조회
        //    예: KAKAO + 123456789
        Optional<Member> existingMember =
                memberRepository.findByProviderAndProviderId(
                        attributes.getProvider(),
                        attributes.getProviderId()
                );

        // 이미 가입된 회원이면 그대로 반환
        if (existingMember.isPresent()) {
            return existingMember.get();
        }

        // 2. 아직 provider/providerId로 가입된 회원이 없다면
        //    이메일 기준으로 중복 회원이 있는지 검사
        //    이유: 이메일 unique 제약이 있기 때문
        //    예를 들어, 구글로 먼저 가입한 이메일을 카카오가 동일하게 쓰고 있다면
        //    현재 정책상 중복 가입으로 예외 처리
        if (attributes.getEmail() != null && !attributes.getEmail().isBlank()) {
            memberRepository.findByEmail(attributes.getEmail())
                    .ifPresent(member -> {
                        throw new OAuth2AuthenticationException(
                                new OAuth2Error("duplicate_email"),
                                "이미 다른 소셜 계정으로 가입된 이메일입니다."
                        );
                    });
        }

        // 3. 닉네임이 없으면 임시 닉네임 생성
        //    예: kakao_123456789
        String nickname = attributes.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = attributes.getProvider().name().toLowerCase() + "_" + attributes.getProviderId();
        }

        // 4. 이메일이 없으면 임시 이메일 생성
        //    OAuth 로그인은 진행해야 하지만 이메일 정보가 없는 경우를 대비
        //    예: kakao_123456789@oauth.local
        String email = attributes.getEmail();
        if (email == null || email.isBlank()) {
            email = attributes.getProvider().name().toLowerCase()
                    + "_" + attributes.getProviderId()
                    + "@oauth.local";
        }

        // 5. OAuth 회원 엔티티 생성
        //    일반 회원가입이 아니라 소셜 로그인 회원으로 생성하는 메서드
        Member newMember = Member.createOAuth(
                email,
                nickname,
                attributes.getProvider(),
                attributes.getProviderId()
        );

        // 6. DB에 저장 후 반환
        return memberRepository.save(newMember);
    }

    /**
     * Spring Security에서 사용할 인증 객체(Principal) 생성
     */
    private OAuth2User createPrincipal(Member member) {
        // Security 세션/인증 객체 안에 담아둘 사용자 정보
        // 이후 컨트롤러나 인증 처리 과정에서 꺼내서 사용할 수 있음
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("memberId", member.getId());
        customAttributes.put("email", member.getEmail());
        customAttributes.put("nickname", member.getNickname());
        customAttributes.put("provider", member.getProvider().name());
        customAttributes.put("role", member.getRole().name());

        // DefaultOAuth2User 생성
        // 1) 권한 목록 설정
        //    예: ROLE_USER
        // 2) 사용자 속성(Map) 설정
        // 3) nameAttributeKey 설정
        //    여기서는 사용자를 대표하는 키를 "memberId"로 지정
        return new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                customAttributes,
                "memberId"
        );
    }
}