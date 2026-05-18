package com.back.team11.domain.cafe.batch

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto
import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.cafe.entity.CafeStatus
import com.back.team11.domain.cafe.entity.CafeType
import com.back.team11.domain.cafe.entity.CongestionLevel
import com.back.team11.domain.cafe.entity.FloorCount
import com.back.team11.domain.cafe.entity.Franchise
import com.back.team11.domain.cafe.repository.CafeRepository
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.math.BigDecimal

class CafeItemProcessor(
    private val cafeRepository: CafeRepository,
) : ItemProcessor<KakaoPlaceDto, Cafe> {

    override fun process(dto: KakaoPlaceDto): Cafe? {
        // 주소 결정: 도로명 주소가 있으면 사용하고, 없으면 지번 주소를 사용
        //takeIf는 조건이 참이면 그 값을 반환하고, 거짓이면 null을 반환, null이면 ?: Elvis 연산자가 dto.addressName을 반환
        val address = dto.roadAddressName.takeIf { it.isNotBlank() } ?: dto.addressName

        // 중복 검사: DB에 이미 같은 이름과 주소를 가진 카페가 있는지 확인
        // 이미 존재한다면 null을 반환하여 Writer(저장) 단계로 넘어가지 않게 처리 (필터링)
        if (cafeRepository.existsByNameAndAddress(dto.placeName, address)) {
            return null
        }

        // 프랜차이즈 판별: 카페 이름을 분석해 프랜차이즈 여부(스타벅스, 투썸 등)를 가져옴
        val franchise = Franchise.from(dto.placeName)

        // 엔티티 생성: DTO 데이터를 바탕으로 실제 DB에 저장할 Cafe 객체 생성
        return Cafe(
            name = dto.placeName,                   // 카페 이름
            address = address,                       // 결정된 주소 (도로명 우선)
            latitude = BigDecimal(dto.y),           // 위도 (문자열 y를 BigDecimal로 변환)
            longitude = BigDecimal(dto.x),          // 경도 (문자열 x를 BigDecimal로 변환)
            phone = dto.phone,                       // 전화번호
            franchise = franchise,                   // 프랜차이즈 정보
            // 프랜차이즈가 NONE이면 개인카페(INDIVIDUAL), 아니면 프랜차이즈(FRANCHISE)
            type = if (franchise == Franchise.NONE) CafeType.INDIVIDUAL else CafeType.FRANCHISE,
            hasToilet = false,                       // API에서 알 수 없는 정보 기본값
            hasOutlet = false,                       // API에서 알 수 없는 정보 기본값
            hasWifi = false,                         // API에서 알 수 없는 정보 기본값
            floorCount = FloorCount.ONE,            // API에서 알 수 없는 정보 기본값
            hasSeparateSpace = false,               // API에서 알 수 없는 정보 기본값
            congestionLevel = CongestionLevel.LOW,  // API에서 알 수 없는 정보 기본값
            status = CafeStatus.APPROVED,           // 즉시 승인 상태로 저장
        )
    }
}