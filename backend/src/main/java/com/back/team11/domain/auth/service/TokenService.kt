package com.back.team11.domain.auth.service

import com.back.team11.global.security.JwtTokenProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TokenService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtTokenProvider: JwtTokenProvider
) {

    companion object {
        private const val REFRESH_PREFIX = "refresh:"
        private const val BLACKLIST_PREFIX = "blacklist:"
    }


    // Refresh Token 저장
    fun saveRefreshToken(memberId: Long, refreshToken: String) {
        redisTemplate.opsForValue()
            .set("$REFRESH_PREFIX$memberId", refreshToken, 7, TimeUnit.DAYS)
    }

    // Refresh Token 조회
    fun getRefreshToken(memberId: Long): String? =
        redisTemplate.opsForValue().get("$REFRESH_PREFIX$memberId")


    // Refresh Token 삭제 (로그아웃)
    fun deleteRefreshToken(memberId: Long) {
        redisTemplate.delete("$REFRESH_PREFIX$memberId")
    }


    // Refresh Token 유효성 검증
    fun isValidRefreshToken(memberId: Long, token: String): Boolean =
        getRefreshToken(memberId) == token

    // AccessToken 블랙리스트 등록 (로그아웃 시)
    fun addBlacklist(accessToken: String, remainingExpiry: Long) {
        redisTemplate.opsForValue()
            .set("$BLACKLIST_PREFIX$accessToken", "logout", remainingExpiry, TimeUnit.MILLISECONDS)
    }

    // 블랙리스트 확인
    fun isBlacklisted(accessToken: String): Boolean =
        redisTemplate.hasKey("$BLACKLIST_PREFIX$accessToken") == true


}