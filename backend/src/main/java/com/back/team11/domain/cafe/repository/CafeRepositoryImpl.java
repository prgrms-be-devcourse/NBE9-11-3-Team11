package com.back.team11.domain.cafe.repository;

import com.back.team11.domain.cafe.dto.AdminCafeSearchCondition;
import com.back.team11.domain.cafe.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.back.team11.domain.cafe.entity.QCafe.cafe;

@Repository
@RequiredArgsConstructor
public class CafeRepositoryImpl implements CafeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Cafe> searchCafes(CafeSearchCondition condition) {
        return queryFactory
                .selectFrom(cafe)
                .where(
                        isApproved(),//AND status = 'APPROVED'
                        latBetween(condition.getSwLat(), condition.getNeLat()),//AND latitude BETWEEN swLat AND neLat
                        lngBetween(condition.getSwLng(), condition.getNeLng()),// AND longitude BETWEEN swLng AND neLng
                        cafeTypeEq(condition.getType()),// AND type = ?
                        franchiseIn(condition.getFranchises()),// AND franchise IN (?)
                        hasToiletEq(condition.getHasToilet()),// AND has_toilet = ?
                        hasOutletEq(condition.getHasOutlet()),// AND has_outlet = ?
                        hasWifiEq(condition.getHasWifi()),// AND has_wifi = ?
                        floorCountIn(condition.getFloorCounts()),// AND floor_count IN (?)
                        hasSeparateSpaceEq(condition.getHasSeparateSpace()),// AND has_separate_space = ?
                        congestionIn(condition.getCongestionLevels())// AND congestion_level IN (?)
                )
                .fetch();// List<Cafe> 반환
    }

    private BooleanExpression isApproved(){
        return cafe.status.eq(CafeStatus.APPROVED);
    }

    private BooleanExpression latBetween(Double swLat, Double neLat) {
        return (swLat != null && neLat != null)
                ? cafe.latitude.between(swLat, neLat) : null;
    }

    private BooleanExpression lngBetween(Double swLng, Double neLng) {
        return (swLng != null && neLng != null)
                ? cafe.longitude.between(swLng, neLng) : null;
    }

    private BooleanExpression cafeTypeEq(CafeType type) {
        return type != null ? cafe.type.eq(type) : null;
    }

    private BooleanExpression franchiseIn(List<Franchise> franchises) {
        return (franchises != null && !franchises.isEmpty()) ? cafe.franchise.in(franchises) : null;
    }

    private BooleanExpression hasToiletEq(Boolean hasToilet) {
        return hasToilet != null ? cafe.hasToilet.eq(hasToilet) : null;
    }

    private BooleanExpression hasOutletEq(Boolean hasOutlet) {
        return hasOutlet != null ? cafe.hasOutlet.eq(hasOutlet) : null;
    }

    private BooleanExpression hasWifiEq(Boolean hasWifi) {
        return hasWifi != null ? cafe.hasWifi.eq(hasWifi) : null;
    }

    private BooleanExpression floorCountIn(List<FloorCount> floorCounts) {
        return (floorCounts != null && !floorCounts.isEmpty()) ? cafe.floorCount.in(floorCounts) : null;
    }

    private BooleanExpression hasSeparateSpaceEq(Boolean hasSeparateSpace) {
        return hasSeparateSpace != null ? cafe.hasSeparateSpace.eq(hasSeparateSpace) : null;
    }

    private BooleanExpression congestionIn(List<CongestionLevel> congestionLevels) {
        return (congestionLevels != null && !congestionLevels.isEmpty()) ? cafe.congestionLevel.in(congestionLevels) : null;
    }


    /// 관리자용 카페 검색 메서드 (페이징 + 최신순 정렬)
    /// 기존에 있는 코드 재사용을 통한 유지보수성 향상
    @Override
    public Page<Cafe> searchAdminCafes(AdminCafeSearchCondition condition, Pageable pageable) {
        // 1. 데이터 조회 쿼리 (페이징 + 최신순 정렬)
        List<Cafe> content = queryFactory
                .selectFrom(cafe)
                .where(
                        statusEq(condition.getStatus()), // AND status = ? (null이면 전체 조회)
                        nameContains(condition.getName())
                )
                .orderBy(cafe.createdAt.desc())         // 최신 등록일 기준 내림차순 정렬
                .offset(pageable.getOffset())           // 페이지 시작점
                .limit(pageable.getPageSize())          // 한 페이지에 보여줄 개수 (15개)
                .fetch();

        // 2. 전체 데이터 개수 카운트 쿼리 (페이징을 위해 필요)
        JPAQuery<Long> countQuery = queryFactory
                .select(cafe.count())
                .from(cafe)
                .where(
                        statusEq(condition.getStatus()),
                        nameContains(condition.getName())
                );

        // 3. Page 객체로 묶어서 반환 (PageableExecutionUtils를 쓰면 최적화된 카운트 쿼리 실행 가능)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 관리자용 상태 일치 여부 확인 메서드
    private BooleanExpression statusEq(CafeStatus status) {
        return status != null ? cafe.status.eq(status) : null;
    }
    // 카페 이름 포함 여부 확인 (null이면 전체 조회)
    private BooleanExpression nameContains(String name) {
        return name != null && !name.isEmpty() ? cafe.name.contains(name) : null;
    }
}


