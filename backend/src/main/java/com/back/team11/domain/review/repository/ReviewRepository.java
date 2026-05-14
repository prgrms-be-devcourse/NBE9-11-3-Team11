package com.back.team11.domain.review.repository;

import com.back.team11.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByMemberIdAndCafeId(Long memberId, Long cafeId);  //리뷰를 생성한지 안한지 체크
    // 전체 조회
    List<Review> findAllByCafeIdOrderByCreatedAtDesc(Long cafeId);
    // 페이징 조회
    Page<Review> findAllByCafeId(Long cafeId, Pageable pageable);

    Optional<Review> findByIdAndCafeId(Long reviewId, Long cafeId);

    // @Modifying은 조회가 아닌 데이터 변경(INSERT, UPDATE, DELETE)임을 나타냄
    // clearAutomatically = true는 벌크 연산 후 영속성 컨텍스트를 비워 데이터 불일치를 방지
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Review r WHERE r.cafe.id = :cafeId")
    void deleteByCafeId(@Param("cafeId") Long cafeId);

}
