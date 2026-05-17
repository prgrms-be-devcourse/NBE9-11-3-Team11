package com.back.team11.domain.auth.repository

import com.back.team11.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

//RefreshToken 엔티티에 접근하기 위한 Repository 인터페이스
// JpaRepository를 상속받아 기본적인 CRUD 메서드를 자동으로 제공받음
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    // 회원 ID(memberId)를 조건으로 리프레시 토큰 정보를 조회
    // → 로그인 시 기존 토큰 있는지 확인할 때 사용
    fun findByMemberId(memberId: Long): RefreshToken?

    // 토큰 문자열(token) 자체를 조건으로 리프레시 토큰 정보를 조회
    // → 재발급 요청 시 토큰 유효성 검증할 때 사용
    fun findByToken(token: String): RefreshToken?

    // 회원 ID(memberId)를 기준으로 리프레시 토큰 데이터를 삭제
    // → 로그아웃 시 사용
    fun deleteByMemberId(memberId: Long)
}
