package com.back.team11.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

/**
 * RestTemplate 빈 설정 클래스입니다.
 *
 * RestTemplate을 각 클래스에서 직접 생성하면 타임아웃 설정이 분산되고
 * Spring의 의존성 주입 원칙에 어긋납니다.
 * 빈으로 등록하면 타임아웃 설정을 한 곳에서 관리하고
 * 필요한 클래스에서 주입받아 재사용할 수 있습니다.
 */
@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(3000) // 연결 타임아웃 3초 - 카카오 서버에 연결 시도하는 최대 대기 시간
            setReadTimeout(5000)    // 읽기 타임아웃 5초 - 카카오 서버로부터 응답 데이터를 읽는 최대 대기 시간
        }
        return RestTemplate(factory)
    }
}