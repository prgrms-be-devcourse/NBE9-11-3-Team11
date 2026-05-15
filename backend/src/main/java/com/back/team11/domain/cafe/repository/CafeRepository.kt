package com.back.team11.domain.cafe.repository

import com.back.team11.domain.cafe.entity.Cafe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CafeRepository : JpaRepository<Cafe, Long>, CafeRepositoryCustom {
    fun existsByNameAndAddress(name: String, address: String): Boolean
}
