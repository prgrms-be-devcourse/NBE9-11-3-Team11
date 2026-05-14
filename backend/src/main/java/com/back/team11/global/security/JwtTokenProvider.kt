package com.back.team11.global.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    // application.yml의 jwt.secret 값 주입 (Base64 인코딩된 시크릿 키)
    @Value("\${jwt.secret}")
    private val secretKey: String,

    // Access Token 만료 시간 (밀리초 단위, 예: 3600000 = 1시간)
    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,

    // Refresh Token 만료 시간 (밀리초 단위, 예: 604800000 = 7일)
    @Value("\${jwt.refresh-token-expiration}")
    val refreshTokenExpiration: Long,
) {

    /**
     * 1. JWT 서명/검증에 사용할 SecretKey 객체 생성
     * 2. application.yml의 Base64 시크릿 키를 디코딩해서 HMAC-SHA 키로 변환
     */
    private fun getSigningKey(): SecretKey {
            val keyBytes = Decoders.BASE64.decode(secretKey)
            return Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * Access Token 생성
     * 페이로드에 memberId(subject), role(권한), 발급시각, 만료시각을 담고 서명
     * 
     * @param memberId 사용자 고유 ID
     * @param role     사용자 권한 (예: "ROLE_USER", "ROLE_ADMIN")
     * @return 서명된 Access Token 문자열
     */
    fun generateAccessToken(memberId: Long, role: String): String {
        return Jwts.builder()
            .subject(memberId.toString()) // 토큰 주체 = 사용자 ID
            .claim("role", role) // 커스텀 클레임: 권한 정보
            .issuedAt(Date()) // 발급 시각
            .expiration(Date(System.currentTimeMillis() + accessTokenExpiration)) // 만료 시각
            .signWith(getSigningKey()) // 시크릿 키로 서명
            .compact() // 최종 JWT 문자열로 직렬화
    }

    /**
     * Refresh Token 생성
     * Access Token 재발급 용도이므로 role 정보는 포함하지 않음
     * 
     * @param memberId 사용자 고유 ID
     * @return 서명된 Refresh Token 문자열
     */
    fun generateRefreshToken(memberId: Long): String {
        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(getSigningKey())
            .compact()
    }

    /**
     * JWT 토큰을 파싱해서 Claims(페이로드) 반환
     * 내부적으로 서명 검증 + 만료 시각 검증이 자동으로 수행됨
     * 변조된 토큰이나 만료된 토큰이 들어오면 예외 발생
     * 
     * @param token JWT 문자열
     * @return 토큰에 담긴 Claims 객체
     */
    fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey()) // 서명 검증에 사용할 키 설정
            .build() // 토큰 파싱 + 서명 검증 + 만료 검증
            .parseSignedClaims(token) // 검증 통과 시 페이로드(Claims) 반환
            .payload // Java의 getPayload() → Kotlin 프로퍼티 접근
    }

    /**
     * 토큰에서 memberId 추출
     * 
     * @param token JWT 문자열
     * @return 사용자 고유 ID
     */
    fun getMemberId(token: String?): Long =
        parseClaims(token!!).subject.toLong()

    /**
     * 토큰에서 role(권한) 추출
     *
     * @param token JWT 문자열
     * @return 사용자 권한 문자열 (예: "ROLE_USER")
     */
    fun getRole(token: String?): String =
        parseClaims(token!!).get("role", String::class.java)

}
