package com.back.team11.domain.cafe.batch

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto
import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.cafe.repository.CafeRepository
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.client.RestTemplate

/**
 * Spring Batch Job 설정 클래스입니다.
 * 카카오 API에서 카페 데이터를 수집하여 DB에 저장하는 배치 Job을 정의합니다.
 *
 * [변경 사항]
 * 기존 CafeApiItemReader → ListItemReader로 교체
 * 카카오 API 호출을 트랜잭션 밖(CafeApiClient.fetchAll())에서 먼저 완료하고
 * 트랜잭션 안에서는 리스트에서 꺼내기만 하는 구조로 변경
 */
@Configuration
class CafeCollectJobConfig(
    private val jobRepository: JobRepository,                    // 배치의 메타데이터(실행 이력, 성공 여부 등)를 기록하는 저장소
    private val transactionManager: PlatformTransactionManager, // 트랜잭션 경계 설정 (실패 시 데이터 롤백 담당)
    private val cafeRepository: CafeRepository,                 // 가공된 Cafe 엔티티를 저장하기 위한 JPA 레포지토리
    private val cafeApiClient: CafeApiClient,                   // 카카오 API 호출 담당 클라이언트 (트랜잭션 밖에서 실행)
) {
    @Bean
    fun cafeCollectJob(): Job =
        JobBuilder("cafeCollectJob", jobRepository) // Job 이름 지정
            .start(cafeCollectStep())               // 실행할 첫 번째 Step 지정
            .build()

    @Bean
    fun cafeCollectStep(): Step =
        StepBuilder("cafeCollectStep", jobRepository)
            // <입력타입, 출력타입>chunk(한 번에 커밋할 개수)
            // 50개 단위로 읽고 가공한 뒤 한꺼번에 저장(Commit)
            // chunk 크기랑 트랜잭션 매니저를 한 번에 넘기던 방식 -> 크기와 매니저 별도로 설정
            .chunk<KakaoPlaceDto, Cafe>(50)
            .transactionManager(transactionManager)
            .reader(cafeApiItemReader())    // 리스트에서 데이터를 하나씩 꺼내옴 (API 호출 없음)
            .processor(cafeItemProcessor()) // 읽어온 DTO를 엔티티로 변환 및 필터링
            .writer(cafeItemWriter())       // 가공된 50개의 데이터를 DB에 일괄 저장
            // 내결함성(Error Skip) 설정
            .faultTolerant()                // 예외 발생 시 스킵/재시도 기능 활성화
            .skip(Exception::class.java)    // 모든 예외에 대해 스킵 허용
            .skipLimit(100)                 // 최대 100건까지 에러 스킵 허용
            .build()

    @Bean
    fun cafeItemWriter(): ItemWriter<Cafe> =
        // chunk.items는 현재 처리 중인 50개의 Cafe 리스트를 반환
        ItemWriter { chunk -> cafeRepository.saveAll(chunk.items) }

    @Bean
    fun cafeApiItemReader(): ListItemReader<KakaoPlaceDto> {
        /**
         * 트랜잭션 시작 전에 카카오 API를 전부 호출하여 리스트로 반환받습니다.
         * ListItemReader는 전달받은 리스트에서 데이터를 하나씩 꺼내주는 역할만 합니다.
         * 트랜잭션 안에서는 카카오 API 호출이 전혀 없으므로 DB 커넥션 점유 문제가 해결됩니다.
         *
         * [변경 전] CafeApiItemReader → read() 호출마다 카카오 API 호출 (트랜잭션 안)
         * [변경 후] ListItemReader  → 리스트에서 꺼내기만 함 (카카오 API는 트랜잭션 밖에서 완료)
         */
        val cafes = cafeApiClient.fetchAll() // 트랜잭션 시작 전 카카오 API 전체 호출
        return ListItemReader(cafes)         // 수집된 리스트를 ListItemReader에 전달
    }

    @Bean
    fun cafeItemProcessor(): CafeItemProcessor =
        CafeItemProcessor(cafeRepository)
}
