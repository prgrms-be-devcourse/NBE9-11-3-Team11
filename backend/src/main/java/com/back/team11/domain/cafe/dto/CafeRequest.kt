package com.back.team11.domain.cafe.dto

import com.back.team11.domain.cafe.entity.CafeType
import com.back.team11.domain.cafe.entity.CongestionLevel
import com.back.team11.domain.cafe.entity.FloorCount
import com.back.team11.domain.cafe.entity.Franchise
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

// 카페 등록/제보 공통 요청 DTO — CafeCreateRequest + CafeReportRequest 통합
// field 사용 이유, 생성자 파라미터에 붙으면  Bean Validation이 인식을 못함
data class CafeRequest(
    @field:NotBlank(message = "카페 이름은 필수입니다.")
    val name: String,

    @field:NotBlank(message = "주소는 필수입니다.")
    val address: String,

    @field:NotNull(message = "위도는 필수입니다.")
    val latitude: BigDecimal,

    @field:NotNull(message = "경도는 필수입니다.")
    val longitude: BigDecimal,

    val phone: String? = null,
    val description: String? = null,

    @field:NotNull(message = "카페 유형은 필수입니다.")
    val type: CafeType,

    @field:NotNull(message = "프랜차이즈 정보는 필수입니다.")
    val franchise: Franchise,

    @field:NotNull(message = "화장실 여부는 필수입니다.")
    val hasToilet: Boolean,

    @field:NotNull(message = "콘센트 여부는 필수입니다.")
    val hasOutlet: Boolean,

    @field:NotNull(message = "와이파이 여부는 필수입니다.")
    val hasWifi: Boolean,

    @field:NotNull(message = "층수는 필수입니다.")
    val floorCount: FloorCount,

    @field:NotNull(message = "독립 공간 여부는 필수입니다.")
    val hasSeparateSpace: Boolean,

    @field:NotNull(message = "혼잡도는 필수입니다.")
    val congestionLevel: CongestionLevel,

    val imageUrl: String? = null,
)
