package com.back.team11.domain.cafe.batch;

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto;
import com.back.team11.domain.cafe.entity.*;
import com.back.team11.domain.cafe.repository.CafeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class CafeItemProcessor implements ItemProcessor<KakaoPlaceDto, Cafe> {

    private final CafeRepository cafeRepository;

    @Override
    public Cafe process(KakaoPlaceDto dto) {
        // 주소 결정: 도로명 주소가 있으면 사용하고, 없으면 지번 주소를 사용
        String address = dto.roadAddressName() != null
                ? dto.roadAddressName()
                : dto.addressName();

        // 중복 검사: DB에 이미 같은 이름과 주소를 가진 카페가 있는지 확인
        // 만약 이미 존재한다면 null을 반환하여, 이 데이터는 Writer(저장) 단계로 넘어가지 않게 처리 (필터링)
        if (cafeRepository.existsByNameAndAddress(dto.placeName(), address)) {
            return null;
        }

        // 프랜차이즈 판별: 카페 이름을 분석해 프랜차이즈 여부(스타벅스, 투썸 등)를 가져옴
        Franchise franchise = Franchise.from(dto.placeName());

        // 엔티티 생성: DTO 데이터를 바탕으로 실제 DB에 저장할 Cafe 객체를 빌더 패턴으로 생성
        return Cafe.builder()
                .name(dto.placeName())                      // 카페 이름
                .address(address)                           // 결정된 주소
                .latitude(new BigDecimal(dto.y()))          // 위도 (문자열 y를 BigDecimal로 변환)
                .longitude(new BigDecimal(dto.x()))         // 경도 (문자열 x를 BigDecimal로 변환)
                .phone(dto.phone())                         // 전화번호
                .franchise(franchise)                       // 프랜차이즈 정보
                // 프랜차이즈가 NONE이면 개인카페(INDIVIDUAL), 아니면 프랜차이즈(FRANCHISE)로 타입 설정
                .type(franchise == Franchise.NONE ? CafeType.INDIVIDUAL : CafeType.FRANCHISE)

                // 기본값 설정: API에서 알 수 없는 정보들은 기본값으로 세팅
                .hasToilet(false)
                .hasOutlet(false)
                .hasWifi(false)
                .floorCount(FloorCount.ONE)
                .hasSeparateSpace(false)
                .congestionLevel(CongestionLevel.LOW)
                .status(CafeStatus.APPROVED)                // 즉시 승인 상태로 저장
                .build();
    }
}