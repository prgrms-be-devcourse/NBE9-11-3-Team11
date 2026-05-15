package com.back.team11.domain.cafe.batch

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto
import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.cafe.repository.CafeRepository
import lombok.RequiredArgsConstructor
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.client.RestTemplate

@Configuration
class CafeCollectJobConfig(
    private val jobRepository: JobRepository,                    // 배치의 메타데이터(실행 이력, 성공 여부 등)를 기록하는 저장소
    private val transactionManager: PlatformTransactionManager, // 트랜잭션 경계 설정 (실패 시 데이터 롤백 담당)
    private val cafeRepository: CafeRepository,                 // 가공된 Cafe 엔티티를 저장하기 위한 JPA 레포지토리

    @Value("\${kakao.rest-api-key}")                            // application.yml에 등록된 카카오 인증키 주입
    private val kakaoApiKey: String,
) {
    @Bean
    fun cafeCollectJob(): Job =
        JobBuilder("cafeCollectJob", jobRepository) // Job 이름 지정
            .start(cafeCollectStep())               // 실행할 첫 번째 Step 지정
            .build()

    // chunk 기반 처리
    @Bean
    fun cafeCollectStep(): Step =
        StepBuilder("cafeCollectStep", jobRepository)
            // <입력타입, 출력타입>chunk(한 번에 커밋할 개수, 트랜잭션 매니저)
            // 50개 단위로 읽고 가공한 뒤 한꺼번에 저장(Commit)
            .chunk<KakaoPlaceDto, Cafe>(50, transactionManager)
            .reader(cafeApiItemReader())     // API를 통해 원본 데이터를 한 줄씩 읽어옴
            .processor(cafeItemProcessor())  // 읽어온 DTO를 엔티티로 변환 및 필터링
            .writer(cafeItemWriter())        // 가공된 50개의 데이터를 DB에 일괄 저장
            // 내결함성(Error Skip) 설정
            .faultTolerant()                 // 예외 발생 시 스킵/재시도 기능 활성화
            .skip(Exception::class.java)     // 모든 예외에 대해 스킵 허용
            .skipLimit(100)                  // 최대 100건까지 에러 스킵 허용
            .build()

    @Bean
    fun cafeItemWriter(): ItemWriter<Cafe> =
        // chunk.items는 현재 처리 중인 50개의 Cafe 리스트를 반환
        ItemWriter { chunk -> cafeRepository.saveAll(chunk.items) }

    @Bean
    fun cafeApiItemReader(): CafeApiItemReader =
        // 데이터가 많아질 경우 빈으로 등록된 커넥션 풀 기반 RestTemplate을 주입받아 쓰는 것이 좋음
        CafeApiItemReader(RestTemplate(), kakaoApiKey)

    @Bean
    fun cafeItemProcessor(): CafeItemProcessor =
        CafeItemProcessor(cafeRepository)
}