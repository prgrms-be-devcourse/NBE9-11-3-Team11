package com.back.team11.domain.cafe.dto

import org.springframework.data.domain.Page

/**  관리자 카페 목록 조회 시 페이징된 정보들을 보여줄 때 꼭 필요한 필수정보들만 표현 */
data class PageResponse<T>(
    val content: List<T>,  // 실제 데이터 목록
    val currentPage: Int,  // 현재 페이지 번호 (1부터 시작)
    val totalPages: Int,  // 전체 페이지 수
    val totalElements: Long // 전체 데이터 개수
) {
    companion object {
        // Spring의 Page 객체를 PageResponse로 변환하는 정적 팩토리 메서드
        fun <T : Any> of(page: Page<T>): PageResponse<T> = PageResponse(
            content = page.content,
            currentPage = page.number + 1, // 0-based index를 1-based index로 변경
            totalPages = page.totalPages,
            totalElements = page.totalElements,
        )
    }
}