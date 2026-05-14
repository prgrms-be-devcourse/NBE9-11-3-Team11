package com.back.team11.global.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// @RequiredArgsConstructor 제거: Kotlin에서 Lombok 생성자 생성이 동작 X -> Spring 주입 X
// EntityManager를 primary constructor로 이동: `= null` 초기화 시 항상 null -> JPAQueryFactory 생성 시 런타임 에러
@Configuration
class QueryDslConfig(
    private val entityManager: EntityManager
) {
    // QueryDsl을 사용하기 위한 JPAQueryFactory 빈 등록
    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}
