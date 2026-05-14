package com.back.team11.domain.cafe.repository;

import com.back.team11.domain.cafe.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Long>, CafeRepositoryCustom {
    boolean existsByNameAndAddress(String name, String address);
}
