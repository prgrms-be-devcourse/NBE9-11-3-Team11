package com.back.team11.domain.cafe.repository

import com.back.team11.domain.cafe.dto.AdminCafeSearchCondition
import com.back.team11.domain.cafe.entity.Cafe
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CafeRepositoryCustom {
    // 기존 사용자용 검색
    fun searchCafes(condition: CafeSearchCondition): List<Cafe>

    // 관리자용 상태 필터링 및 페이징 검색 (Page 반환)
    fun searchAdminCafes(condition: AdminCafeSearchCondition, pageable: Pageable): Page<Cafe>
}
