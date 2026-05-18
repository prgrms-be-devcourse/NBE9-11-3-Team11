package com.back.team11.global.dto

import org.springframework.data.domain.Page

// 리팩토링: DTO는 data class로 유지, 기존 기록용 주석 제거
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

/* 기존 JvmStatic 제거
   toPageResponse() 확장함수 정의를 통해 재사용성, 의도 명확화
   Page<t> 가 receiver type으로 변경 -> 함수를 자기 메서드처럼 호출
 */
fun <T : Any> Page<T>.toPageResponse(): PageResponse<T> = PageResponse(
    content = content,
    page = number + 1,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages,
    hasNext = hasNext(),
)
