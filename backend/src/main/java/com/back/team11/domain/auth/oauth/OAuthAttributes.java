package com.back.team11.domain.auth.oauth;

import com.back.team11.domain.member.entity.Provider;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes { // OAuth 제공자마다 다른 사용자 정보 형식을 하나로 통일하는 역할

    // 어떤 소셜 로그인 제공자인지 저장
    // 예: KAKAO, GOOGLE, NAVER
    private final Provider provider;

    // OAuth 제공자에서 내려주는 사용자 고유 ID
    // 예: 카카오의 회원 식별값
    private final String providerId;

    // 사용자 이메일
    private final String email;

    // 사용자 닉네임
    private final String nickname;

    // OAuth 제공자로부터 받은 원본 전체 데이터
    // 디버깅이나 추가 정보 활용 시 사용할 수 있음
    private final Map<String, Object> attributes;

    public OAuthAttributes(
            Provider provider,
            String providerId,
            String email,
            String nickname,
            Map<String, Object> attributes
    ){
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.attributes = attributes;
    }

    /**
     * registrationId(소셜 로그인 제공자 이름)에 따라
     * 알맞은 파싱 메서드로 분기하는 정적 팩토리 메서드
     *
     * 예:
     * - "kakao" -> ofKakao(attributes)
     * - 추후 "google", "naver"도 추가 가능
     */
    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes){
        return switch(registrationId.toLowerCase()){
            case "kakao" -> ofKakao(attributes);

            // 현재는 카카오만 지원
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + registrationId);
        };
    }

    /**
     * 카카오 사용자 정보 응답을 우리 서비스 공통 형식으로 변환
     *
     * 카카오 응답 예시 구조:
     * {
     *   "id": 123456789,
     *   "kakao_account": {
     *       "email": "test@test.com",
     *       "profile": {
     *           "nickname": "홍길동"
     *       }
     *   }
     * }
     */
    private static OAuthAttributes ofKakao(Map<String, Object> attributes){ // 카카오 파싱
        // 최상위 "id" 값 추출
        // 카카오의 사용자 고유 식별값
        Object idObj = attributes.get("id");
        String providerId = idObj != null ? String.valueOf(idObj) : null;

        // 중첩 객체 kakao_account 추출
        Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");

        // kakao_account 안의 profile 객체 추출
        Map<String, Object> profile = getMap(kakaoAccount, "profile");

        // 이메일 추출
        String email = getString(kakaoAccount, "email");

        // 닉네임 추출
        String nickname = getString(profile, "nickname");

        // 카카오 응답을 우리 서비스 공통 DTO로 변환해서 반환
        return new OAuthAttributes(
                Provider.KAKAO,
                providerId,
                email,
                nickname,
                attributes
        );
    }

    /**
     * Map 안에서 특정 key의 값을 꺼내되,
     * 그 값이 Map 형태가 아니면 빈 Map 반환
     *
     * 이유:
     * OAuth 응답은 중첩 JSON 구조이기 때문에
     * 안전하게 형변환해서 꺼내기 위한 헬퍼 메서드
     */
    @SuppressWarnings("unchecked") // 중첩 JSON 안전하게 꺼내기 위한 메서드
    private static Map<String, Object> getMap(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);

        // 값이 없으면 빈 Map 반환
        if (value == null) {
            return Map.of();
        }

        // 값이 Map 타입이 아니어도 빈 Map 반환
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        // Object -> Map<String, Object> 형변환
        return (Map<String, Object>) map;
    }

    /**
     * Map 안에서 특정 key의 값을 문자열로 꺼내는 헬퍼 메서드
     *
     * 값이 없으면 null 반환
     */
    private static String getString(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}