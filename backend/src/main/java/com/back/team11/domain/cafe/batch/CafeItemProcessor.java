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

        //임시 메서드로 변경
        return Cafe.createByBatch(
                dto.placeName(),
                address,
                new BigDecimal(dto.y()),
                new BigDecimal(dto.x()),
                dto.phone(),
                franchise
        );
    }
}