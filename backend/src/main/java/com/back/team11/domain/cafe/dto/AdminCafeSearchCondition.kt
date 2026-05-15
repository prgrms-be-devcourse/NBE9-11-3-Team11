package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.entity.CafeStatus

// 관리자 필터링 조건 (현재는 상태만, 추후 이름이나 타입 등 추가 가능)
data class AdminCafeSearchCondition(
    var status: CafeStatus? = null,
    var name: String? = null, // 카페 이름 검색 조건
)