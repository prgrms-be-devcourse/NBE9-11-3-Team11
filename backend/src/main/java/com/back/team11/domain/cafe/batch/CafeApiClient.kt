package com.back.team11.domain.cafe.batch

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto
import com.back.team11.domain.cafe.batch.dto.KakaoSearchResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * 카카오 API를 호출하여 카페 데이터를 수집하는 클라이언트 클래스입니다.
 *
 * [기존 문제]
 * CafeApiItemReader에서 카카오 API를 호출했을 때, Spring Batch의 chunk 처리 특성상
 * Reader도 트랜잭션 범위 안에 포함됩니다.
 * 즉, 카카오 API를 호출하는 동안 트랜잭션이 열려있고 DB 커넥션을 점유하게 됩니다.
 * API 응답이 느리거나 타임아웃이 발생하면 전체 트랜잭션 장애로 번질 수 있습니다.
 *
 * [해결 방법]
 * 카카오 API 호출을 트랜잭션 밖으로 완전히 분리합니다.
 * 이 클래스에서 카카오 API를 모두 호출하여 리스트로 반환하고,
 * CafeCollectJobConfig에서 ListItemReader에 해당 리스트를 넘겨줍니다.
 * 트랜잭션 안에서는 리스트에서 데이터를 꺼내기만 하므로 DB 커넥션 점유 문제가 해결됩니다.
 */
@Component // Spring이 이 클래스를 빈으로 관리하도록 등록
class CafeApiClient(
    @Value("\${kakao.rest-api-key}") // application.yml에 등록된 카카오 인증키 주입
    private val kakaoApiKey: String,
    private val restTemplate: RestTemplate, // RestTemplateConfig에서 빈으로 등록된 RestTemplate 주입
    // 타임아웃 설정이 한 곳(RestTemplateConfig)에서 관리됨
) {
    companion object {
        // 클래스 전용 로거 - 카카오 API 호출 현황을 로그로 기록
        private val logger = LoggerFactory.getLogger(CafeApiClient::class.java)
    }

    /**
     * 카카오 API로 카페를 검색할 서울의 주요 상권 15곳의 좌표 목록입니다.
     * 각 배열은 [경도(x), 위도(y)] 순서입니다.
     * 각 좌표를 중심으로 반경 2km 이내의 카페를 수집합니다.
     */
    private val coordGrid = listOf(
        doubleArrayOf(126.9784, 37.5665), // 시청
        doubleArrayOf(127.0276, 37.4979), // 강남
        doubleArrayOf(126.9215, 37.5519), // 홍대
        doubleArrayOf(127.1000, 37.5133), // 잠실
        doubleArrayOf(126.9882, 37.5172), // 이태원
        doubleArrayOf(127.0558, 37.5446), // 성수
        doubleArrayOf(126.9373, 37.5552), // 신촌
        doubleArrayOf(127.0490, 37.5044), // 선릉
        doubleArrayOf(127.0372, 37.5620), // 왕십리
        doubleArrayOf(126.9243, 37.5217), // 여의도
        doubleArrayOf(127.0632, 37.5088), // 삼성
        doubleArrayOf(126.9297, 37.4842), // 신림
        doubleArrayOf(127.0614, 37.6542), // 노원
        doubleArrayOf(126.9087, 37.4192), // 관악
        doubleArrayOf(127.0693, 37.5405), // 건대
    )

    /**
     * 모든 좌표에 대해 카카오 API를 호출하여 카페 데이터를 전부 수집합니다.
     *
     * 이 메서드는 Spring Batch 트랜잭션이 시작되기 전에 호출됩니다.
     * 즉, 카카오 API 호출이 완전히 끝난 후에 트랜잭션이 시작되므로
     * DB 커넥션을 점유하지 않습니다.
     *
     * @return 수집된 카페 데이터 전체 리스트
     */
    fun fetchAll(): List<KakaoPlaceDto> {
        // 수집된 카페 데이터를 담을 리스트
        val result = mutableListOf<KakaoPlaceDto>()

        // 15개 좌표를 순서대로 순회하며 카페 데이터 수집
        for ((gridIndex, coord) in coordGrid.withIndex()) {
            var currentPage = 1 // 각 좌표마다 1페이지부터 시작

            // 해당 좌표의 마지막 페이지까지 반복 호출
            while (true) {
                logger.info("카카오 API 호출 - gridIndex: {}, page: {}", gridIndex, currentPage)

                // 카카오 키워드 검색 API URL 조립
                val url = UriComponentsBuilder
                    .fromUri(URI.create("https://dapi.kakao.com/v2/local/search/keyword.json"))
                    .queryParam("query", "카페")              // 검색어
                    .queryParam("category_group_code", "CE7") // 카페 카테고리 코드
                    .queryParam("x", coord[0])                // 경도
                    .queryParam("y", coord[1])                // 위도
                    .queryParam("radius", 2000)               // 반경 2km
                    .queryParam("page", currentPage)          // 현재 페이지 번호
                    .queryParam("size", 15)                   // 페이지당 결과 수
                    .toUriString()

                // 카카오 API 인증 헤더 설정
                val headers = HttpHeaders()
                headers.set("Authorization", "KakaoAK $kakaoApiKey")

                // 카카오 API 호출
                val response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    KakaoSearchResponse::class.java,
                ).body

                // 응답이 없거나 결과가 없거나 마지막 페이지면 다음 좌표로 이동
                if (response == null || response.documents.isEmpty() || response.meta.isEnd) break

                // 수집된 카페 데이터를 결과 리스트에 추가
                result.addAll(response.documents)
                currentPage++ // 다음 페이지로 이동
            }
        }

        logger.info("카카오 API 전체 수집 완료 - 총 {}건", result.size)
        return result
    }
}