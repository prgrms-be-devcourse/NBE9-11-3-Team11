package com.back.team11.global.dto

import org.springframework.data.domain.Page

// Java record 대신 Kotlin data class 사용
// @JvmRecord는 kapt 스텁 생성 시 extends Record 충돌로 사용 불가
// 스텁? kapt가 Kotlin 코드를 Java가 읽을 수 있도록 임시로 생성하는 Java 코드
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
) {
    companion object {
        // @JvmStatic으로 Java에서 PageResponse.from() 형태로 호출 가능하게 함
        // Kotlin에서 Any는 Java의 Object랑 같은데 null 널이 안됨
        @JvmStatic
        fun <T : Any> from(page: Page<T>): PageResponse<T> = PageResponse(
            content = page.content,
            page = page.number + 1,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            hasNext = page.hasNext(),
        )
    }
}
