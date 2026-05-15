package com.back.team11.domain.review.repository

import com.back.team11.domain.review.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ReviewRepository : JpaRepository<Review, Long> {
    fun existsByMemberIdAndCafeId(memberId: Long, cafeId: Long): Boolean //리뷰를 생성한지 안한지 체크

    // 전체 조회
    fun findAllByCafeIdOrderByCreatedAtDesc(cafeId: Long): List<Review>

    // 페이징 조회
    fun findAllByCafeId(cafeId: Long, pageable: Pageable): Page<Review>

    fun findByIdAndCafeId(reviewId: Long, cafeId: Long): Optional<Review>

    // @Modifying은 조회가 아닌 데이터 변경(INSERT, UPDATE, DELETE)임을 나타냄
    // clearAutomatically = true는 벌크 연산 후 영속성 컨텍스트를 비워 데이터 불일치를 방지
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Review r WHERE r.cafe.id = :cafeId")
    fun deleteByCafeId(@Param("cafeId") cafeId: Long)


    // 전체 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.member JOIN FETCH r.cafe WHERE r.cafe.id = :cafeId ORDER BY r.createdAt DESC")
    fun findAllByCafeIdWithFetch(@Param("cafeId") cafeId: Long): MutableList<Review>

    // 페이징 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.member JOIN FETCH r.cafe WHERE r.cafe.id = :cafeId")
    fun findAllByCafeIdWithFetch(@Param("cafeId") cafeId: Long, pageable: Pageable): Page<Review>

    @Query("SELECT r FROM Review r JOIN FETCH r.member JOIN FETCH r.cafe WHERE r.id = :reviewId AND r.cafe.id = :cafeId")
    fun findByIdAndCafeIdWithFetch(
        @Param("reviewId") reviewId: Long,
        @Param("cafeId") cafeId: Long
    ): Optional<Review>
}
