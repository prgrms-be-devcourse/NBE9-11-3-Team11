package com.back.team11.domain.auth.oauth

import com.back.team11.domain.member.entity.Provider

data class OAuthAttributes(
    val provider: Provider,
    val providerId: String,
    val email: String?,
    val nickname: String,
    val attributes: Map<String, Any?>
) {
    companion object {
        /**
         * registrationId(소셜 로그인 제공자 이름)에 따라
         * 알맞은 파싱 메서드로 분기하는 정적 팩토리 메서드
         *
         * 예:
         * - "kakao" -> ofKakao(attributes)
         * - 추후 "google", "naver"도 추가 가능
         */
        fun of(registrationId: String, attributes: Map<String, Any?>): OAuthAttributes =
            when (registrationId.lowercase()) {
                "kakao" -> ofKakao(attributes)
                else -> throw IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: $registrationId")
            }

        /**
         * 카카오 사용자 정보 응답을 우리 서비스 공통 형식으로 변환
         *
         * 카카오 응답 예시 구조:
         * {
         * "id": 123456789,
         * "kakao_account": {
         * "email": "test@test.com",
         * "profile": {
         * "nickname": "홍길동"
         * }
         * }
         * }
         */
        private fun ofKakao(attributes: Map<String, Any?>): OAuthAttributes {

            val kakaoAccount = attributes.getMap("kakao_account")
            val profile = kakaoAccount.getMap("profile")

            return OAuthAttributes(
                provider = Provider.KAKAO,
                providerId = attributes["id"]?.toString()
                    ?: throw IllegalArgumentException("카카오 providerId가 없습니다."),
                email = kakaoAccount.getString("email"),
                nickname = profile.getString("nickname")
                    ?: throw IllegalArgumentException("카카오 nickname이 없습니다."),
                attributes = attributes
            )
        }

        /**
         * Map 안에서 특정 key의 값을 꺼내되,
         * 그 값이 Map 형태가 아니면 빈 Map 반환
         *
         * 이유:
         * OAuth 응답은 중첩 JSON 구조이기 때문에
         * 안전하게 형변환해서 꺼내기 위한 헬퍼 메서드
         */
        @Suppress("UNCHECKED_CAST")
        private fun Map<String, Any?>.getMap(key: String): Map<String, Any?> =
            this[key] as? Map<String, Any?> ?: emptyMap()


        private fun Map<String, Any?>.getString(key: String): String? =
            this[key]?.toString()

    }
}