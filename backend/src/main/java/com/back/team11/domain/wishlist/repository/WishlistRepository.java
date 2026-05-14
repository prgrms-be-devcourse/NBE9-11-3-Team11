package com.back.team11.domain.wishlist.repository;

import com.back.team11.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    boolean existsByMemberIdAndCafeId(Long id, Long cafeId);

    void deleteByMemberIdAndCafeId(Long id, Long id1);

    //Lazy 로딩으로 인한 N+1 문제를 해결하기 위한 Join fetch 도입
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.cafe WHERE w.member.id = :memberId")
    Page<Wishlist> findAllByMemberIdWithCafe(@Param("memberId") Long memberId,  Pageable pageable);

    long countByCafeId(Long cafeId);


    // @Modifying은 조회가 아닌 데이터 변경(INSERT, UPDATE, DELETE)임을 나타냄
    // clearAutomatically = true는 벌크 연산 후 영속성 컨텍스트를 비워 데이터 불일치를 방지
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Wishlist w WHERE w.cafe.id = :cafeId")
    void deleteByCafeId(@Param("cafeId") Long cafeId);

}
