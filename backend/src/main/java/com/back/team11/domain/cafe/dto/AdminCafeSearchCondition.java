package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.CafeStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCafeSearchCondition {
    // 관리자 필터링 조건 (현재는 상태만, 추후 이름이나 타입 등 추가 가능)
    private CafeStatus status;
    private String name;  // 카페 이름 검색 조건 추가
}