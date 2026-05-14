package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// PATCH 방식 - 전송된 필드만 수정, null인 필드는 기존값 유지
@Getter
@NoArgsConstructor
public class CafeUpdateRequest {

    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String description;
    private CafeType type;
    private Franchise franchise;
    private Boolean hasToilet;
    private Boolean hasOutlet;
    private Boolean hasWifi;
    private FloorCount floorCount;
    private Boolean hasSeparateSpace;
    private CongestionLevel congestionLevel;
    private String imageUrl;
}
