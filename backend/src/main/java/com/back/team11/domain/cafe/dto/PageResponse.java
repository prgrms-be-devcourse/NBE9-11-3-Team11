package com.back.team11.domain.cafe.dto;

import org.springframework.data.domain.Page;
import java.util.List;

///  관리자 카페 목록 조회 시 페이징된 정보들을 보여줄 때 꼭 필요한 필수정보들만 표현
public record PageResponse<T>(
        List<T> content,       // 실제 데이터 목록
        int currentPage,       // 현재 페이지 번호 (1부터 시작)
        int totalPages,        // 전체 페이지 수
        long totalElements     // 전체 데이터 개수
) {
    // Spring의 Page 객체를 PageResponse로 변환하는 정적 팩토리 메서드
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1, // 클라이언트 친화적으로 0-based index를 1-based index로 변경
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}