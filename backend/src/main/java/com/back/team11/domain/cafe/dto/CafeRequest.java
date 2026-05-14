package com.back.team11.domain.cafe.dto;

import com.back.team11.domain.cafe.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

// 카페 등록/제보 공통 요청 DTO — CafeCreateRequest + CafeReportRequest 통합
@Getter
@NoArgsConstructor
public class CafeRequest {

    @NotBlank(message = "카페 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "위도는 필수입니다.")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다.")
    private BigDecimal longitude;

    private String phone;
    private String description;

    @NotNull(message = "카페 유형은 필수입니다.")
    private CafeType type;

    @NotNull(message = "프랜차이즈 정보는 필수입니다.")
    private Franchise franchise;

    @NotNull(message = "화장실 여부는 필수입니다.")
    private Boolean hasToilet;

    @NotNull(message = "콘센트 여부는 필수입니다.")
    private Boolean hasOutlet;

    @NotNull(message = "와이파이 여부는 필수입니다.")
    private Boolean hasWifi;

    @NotNull(message = "층수는 필수입니다.")
    private FloorCount floorCount;

    @NotNull(message = "독립 공간 여부는 필수입니다.")
    private Boolean hasSeparateSpace;

    @NotNull(message = "혼잡도는 필수입니다.")
    private CongestionLevel congestionLevel;

    private String imageUrl;
}
