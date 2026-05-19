package com.back.team11.domain.cafe.repository

import com.back.team11.domain.cafe.dto.AdminCafeSearchCondition
import com.back.team11.domain.cafe.entity.*
import com.back.team11.domain.cafe.entity.QCafe.cafe
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class CafeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CafeRepositoryCustom {

    override fun searchCafes(condition: CafeSearchCondition): List<Cafe> =
        queryFactory
            .selectFrom(cafe)
            .where(
                isApproved(),                                           // AND status = 'APPROVED'
                latBetween(condition.swLat, condition.neLat),          // AND latitude BETWEEN swLat AND neLat
                lngBetween(condition.swLng, condition.neLng),          // AND longitude BETWEEN swLng AND neLng
                cafeTypeEq(condition.type),                            // AND type = ?
                franchiseIn(condition.franchises),                     // AND franchise IN (?)
                hasToiletEq(condition.hasToilet),                      // AND has_toilet = ?
                hasOutletEq(condition.hasOutlet),                      // AND has_outlet = ?
                hasWifiEq(condition.hasWifi),                          // AND has_wifi = ?
                floorCountIn(condition.floorCounts),                   // AND floor_count IN (?)
                hasSeparateSpaceEq(condition.hasSeparateSpace),        // AND has_separate_space = ?
                congestionIn(condition.congestionLevels),              // AND congestion_level IN (?)
            )
            .fetch()

    private fun isApproved(): BooleanExpression =
        cafe.status.eq(CafeStatus.APPROVED)

    private fun latBetween(swLat: Double?, neLat: Double?): BooleanExpression? =
        if (swLat != null && neLat != null) cafe.latitude.between(swLat, neLat) else null

    private fun lngBetween(swLng: Double?, neLng: Double?): BooleanExpression? =
        if (swLng != null && neLng != null) cafe.longitude.between(swLng, neLng) else null

    private fun cafeTypeEq(type: CafeType?): BooleanExpression? =
        type?.let { cafe.type.eq(it) }

    private fun franchiseIn(franchises: List<Franchise>?): BooleanExpression? =
        if (!franchises.isNullOrEmpty()) cafe.franchise.`in`(franchises) else null

    private fun hasToiletEq(hasToilet: Boolean?): BooleanExpression? =
        hasToilet?.let { cafe.hasToilet.eq(it) }

    private fun hasOutletEq(hasOutlet: Boolean?): BooleanExpression? =
        hasOutlet?.let { cafe.hasOutlet.eq(it) }

    private fun hasWifiEq(hasWifi: Boolean?): BooleanExpression? =
        hasWifi?.let { cafe.hasWifi.eq(it) }

    private fun floorCountIn(floorCounts: List<FloorCount>?): BooleanExpression? =
        if (!floorCounts.isNullOrEmpty()) cafe.floorCount.`in`(floorCounts) else null

    private fun hasSeparateSpaceEq(hasSeparateSpace: Boolean?): BooleanExpression? =
        hasSeparateSpace?.let { cafe.hasSeparateSpace.eq(it) }

    private fun congestionIn(congestionLevels: List<CongestionLevel>?): BooleanExpression? =
        if (!congestionLevels.isNullOrEmpty()) cafe.congestionLevel.`in`(congestionLevels) else null

    // 관리자용 카페 검색 메서드 (페이징 + 최신순 정렬)
    override fun searchAdminCafes(condition: AdminCafeSearchCondition, pageable: Pageable): Page<Cafe> {
        // 1. 데이터 조회 쿼리 (페이징 + 최신순 정렬)
        val content = queryFactory
            .selectFrom(cafe)
            .where(
                statusEq(condition.status),     // AND status = ? (null이면 전체 조회)
                nameContains(condition.name),
            )
            .orderBy(cafe.createdAt.desc())     // 최신 등록일 기준 내림차순 정렬
            .offset(pageable.offset)            // 페이지 시작점
            .limit(pageable.pageSize.toLong())  // 한 페이지에 보여줄 개수
            .fetch()

        // 2. 전체 데이터 개수 카운트 쿼리
        val countQuery = queryFactory
            .select(cafe.count())
            .from(cafe)
            .where(
                statusEq(condition.status),
                nameContains(condition.name),
            )

        // 3. Page 객체로 묶어서 반환 (최적화된 카운트 쿼리 실행)
        //countQuery::fetchOne 반환 타입이 Long? 이라 타입 불일치
        //countQuery.fetchOne() ?: 0L로 수정
        return PageableExecutionUtils.getPage(content, pageable) {countQuery.fetchOne() ?: 0L}
    }

    // 관리자용 상태 일치 여부 확인
    private fun statusEq(status: CafeStatus?): BooleanExpression? =
        status?.let { cafe.status.eq(it) }

    // 카페 이름 포함 여부 확인 (null이면 전체 조회)
    private fun nameContains(name: String?): BooleanExpression? =
        if (!name.isNullOrEmpty()) cafe.name.containsIgnoreCase(name) else null
}


