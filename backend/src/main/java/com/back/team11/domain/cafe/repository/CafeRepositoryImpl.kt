package com.back.team11.domain.cafe.repository

import com.back.team11.domain.cafe.dto.AdminCafeSearchCondition
import com.back.team11.domain.cafe.entity.*
import com.back.team11.domain.cafe.entity.QCafe.cafe
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class CafeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val entityManager: EntityManager,  // Native Query용 추가
) : CafeRepositoryCustom {

    override fun searchCafes(condition: CafeSearchCondition): List<Cafe> {
        // 좌표 범위가 없으면 기존 QueryDSL 방식으로 fallback
        if (condition.swLat == null || condition.neLat == null ||
            condition.swLng == null || condition.neLng == null) {
            return searchCafesQueryDsl(condition)
        }
        return searchCafesPostGis(condition)
    }

    // PostGIS ST_Within + MBR(Minimum Bounding Rectangle) 방식
    private fun searchCafesPostGis(condition: CafeSearchCondition): List<Cafe> {
        val sql = buildString {
            append("""
                SELECT c.* FROM cafe c
                WHERE c.status = 'APPROVED'
                AND ST_Within(
                    c.location,
                    ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
                )
            """)
            condition.type?.let { append(" AND c.type = :type") }
            condition.hasToilet?.let { append(" AND c.has_toilet = :hasToilet") }
            condition.hasOutlet?.let { append(" AND c.has_outlet = :hasOutlet") }
            condition.hasWifi?.let { append(" AND c.has_wifi = :hasWifi") }
            condition.hasSeparateSpace?.let { append(" AND c.has_separate_space = :hasSeparateSpace") }
            if (!condition.franchises.isNullOrEmpty()) {
                append(" AND c.franchise IN (:franchises)")
            }
            if (!condition.floorCounts.isNullOrEmpty()) {
                append(" AND c.floor_count IN (:floorCounts)")
            }
            if (!condition.congestionLevels.isNullOrEmpty()) {
                append(" AND c.congestion_level IN (:congestionLevels)")
            }
        }

        val query = entityManager.createNativeQuery(sql, Cafe::class.java)

        // 좌표 바인딩
        query.setParameter("swLng", condition.swLng)
        query.setParameter("swLat", condition.swLat)
        query.setParameter("neLng", condition.neLng)
        query.setParameter("neLat", condition.neLat)

        // 옵셔널 파라미터 바인딩
        condition.type?.let { query.setParameter("type", it.name) }
        condition.hasToilet?.let { query.setParameter("hasToilet", it) }
        condition.hasOutlet?.let { query.setParameter("hasOutlet", it) }
        condition.hasWifi?.let { query.setParameter("hasWifi", it) }
        condition.hasSeparateSpace?.let { query.setParameter("hasSeparateSpace", it) }
        if (!condition.franchises.isNullOrEmpty()) {
            query.setParameter("franchises", condition.franchises.map { it.name })
        }
        if (!condition.floorCounts.isNullOrEmpty()) {
            query.setParameter("floorCounts", condition.floorCounts.map { it.name })
        }
        if (!condition.congestionLevels.isNullOrEmpty()) {
            query.setParameter("congestionLevels", condition.congestionLevels.map { it.name })
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<Cafe>
    }

    // 좌표 없을 때 fallback (기존 로직)
    private fun searchCafesQueryDsl(condition: CafeSearchCondition): List<Cafe> =
        queryFactory
            .selectFrom(cafe)
            .where(
                isApproved(),
                cafeTypeEq(condition.type),
                franchiseIn(condition.franchises),
                hasToiletEq(condition.hasToilet),
                hasOutletEq(condition.hasOutlet),
                hasWifiEq(condition.hasWifi),
                floorCountIn(condition.floorCounts),
                hasSeparateSpaceEq(condition.hasSeparateSpace),
                congestionIn(condition.congestionLevels),
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


