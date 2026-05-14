package com.back.team11.domain.cafe.batch;

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto;
import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.cafe.repository.CafeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;


@Configuration
@RequiredArgsConstructor
public class CafeCollectJobConfig {

    private final JobRepository jobRepository;           // 배치의 메타데이터(실행 이력, 성공 여부 등)를 기록하는 저장소
    private final PlatformTransactionManager transactionManager; // 트랜잭션 경계 설정 (실패 시 데이터 롤백 담당)
    private final CafeRepository cafeRepository;         // 가공된 Cafe 엔티티를 저장하기 위한 JPA 레포지토리

    @Value("${kakao.rest-api-key}")                      // application.yml 등에 등록된 카카오 인증키 주입
    private String kakaoApiKey;

    @Bean
    public Job cafeCollectJob() {
        return new JobBuilder("cafeCollectJob", jobRepository) // Job 이름 지정
                .start(cafeCollectStep())                      // 실행할 첫 번째 Step 지정
                .build();
    }

    // chunk 기반 처리
    @Bean
    public Step cafeCollectStep() {
        // StepBuilder를 통해 "cafeCollectStep"이라는 이름의 단계를 생성합니다.
        return new StepBuilder("cafeCollectStep", jobRepository)

                // <입력타입, 출력타입>chunk(한 번에 커밋할 개수, 트랜잭션 매니저)
                // 50개 단위로 읽고 가공한 뒤 한꺼번에 저장(Commit)합니다.
                .<KakaoPlaceDto, Cafe>chunk(50, transactionManager)

                .reader(cafeApiItemReader())     // API를 통해 원본 데이터를 한 줄씩 읽어옴
                .processor(cafeItemProcessor())   // [Processor] 읽어온 DTO를 엔티티로 변환 및 필터링
                .writer(cafeItemWriter())         // [Writer] 가공된 50개의 데이터를 DB에 일괄 저장

                // 내결함성(Error Skip) 설정'SimpleStepBuilder' 경고(Deprecated)가 뜰 수 있으나,
                // 이는 스프링 배치 5.1+ 버전의 빌더 구조 변경 때문이며 코드상의 오류는 아님
                .faultTolerant()                  // 예외 발생 시 스킵/재시도 기능을 활성화
                .skip(Exception.class)            // 모든 예외에 대해 스킵 허용
                .skipLimit(100)                   // 전체 작업 중 에러 발생 시 최대 100건까지는 그냥 넘어가고 진행
                .build();
    }


    @Bean
    public ItemWriter<Cafe> cafeItemWriter() {
        // chunk.getItems()는 현재 처리 중인 50개의 Cafe 리스트를 반환
        return chunk -> cafeRepository.saveAll(chunk.getItems());
    }

    @Bean
    public CafeApiItemReader cafeApiItemReader() {
        // 현재는 매번 new RestTemplate()으로 생성
        // 데이터가 많아질 경우 빈(Bean)으로 등록된 커넥션 풀 기반 RestTemplate을 주입받아 쓰는 것이 좋음
        return new CafeApiItemReader(new RestTemplate(), kakaoApiKey);
    }

    @Bean
    public CafeItemProcessor cafeItemProcessor() {
        return new CafeItemProcessor(cafeRepository);
    }
}