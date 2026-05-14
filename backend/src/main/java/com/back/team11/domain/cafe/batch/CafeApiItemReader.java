package com.back.team11.domain.cafe.batch;

import com.back.team11.domain.cafe.batch.dto.KakaoPlaceDto;
import com.back.team11.domain.cafe.batch.dto.KakaoSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * Spring Batch에서 데이터를 읽어오는 'Reader' 역할을 하는 클래스입니다.
 * 카카오 API를 호출하여 카페 정보를 가져오며, ItemReader 인터페이스를 구현합니다.
 */
@Slf4j
public class CafeApiItemReader implements ItemReader<KakaoPlaceDto> {

    private final RestTemplate restTemplate; // HTTP 통신을 위한 도구
    private final String kakaoApiKey;        // 카카오 API 인증 키

    // 데이터를 수집할 중심점(위도, 경도) 리스트입니다. (서울의 주요 상권 5곳)
    private final List<double[]> coordGrid = Arrays.asList(
            new double[]{126.9784, 37.5665}, // 시청 지점
            new double[]{127.0276, 37.4979}, // 강남 지점
            new double[]{126.9215, 37.5519}, // 홍대 지점
            new double[]{127.1000, 37.5133}, // 잠실
            new double[]{126.9882, 37.5172}, // 이태원 지점
            new double[]{127.0558, 37.5446}, // 성수
            new double[]{126.9373, 37.5552}, // 신촌
            new double[]{127.0490, 37.5044}, // 선릉
            new double[]{127.0372, 37.5620}, // 왕십리
            new double[]{126.9243, 37.5217}, // 여의도
            new double[]{127.0632, 37.5088}, // 삼성
            new double[]{126.9297, 37.4842}, // 신림
            new double[]{127.0614, 37.6542}, // 노원
            new double[]{126.9087, 37.4192}, // 관악
            new double[]{127.0693, 37.5405}  // 건대

    );

    private int gridIndex = 0;               // 현재 몇 번째 좌표(coordGrid)를 조회 중인지 기록
    private int currentPage = 1;             // 현재 좌표에서 몇 번째 페이지를 조회 중인지 기록
    private boolean isDone = false;          // 모든 좌표의 데이터를 다 가져왔는지 확인하는 깃발
    private final Queue<KakaoPlaceDto> buffer = new LinkedList<>(); // API로 가져온 15개 데이터를 하나씩 배출하기 위한 임시 저장소

    // 생성자: 필요한 도구와 API 키를 외부(설정 클래스)로부터 주입받습니다.
    public CafeApiItemReader(RestTemplate restTemplate, String kakaoApiKey) {
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

     //Spring Batch가 데이터를 하나씩 읽을 때 호출하는 메서드
    @Override
    public KakaoPlaceDto read() {
        // 버퍼(임시 저장소)에 카페 정보가 남아있다면, 하나를 꺼내서 즉시 반환
        if (!buffer.isEmpty()) return buffer.poll();

        // 모든 좌표 조사가 끝났다면(isDone), null을 반환하여 배치를 종료
        if (isDone) return null;

        // 버퍼가 비어있다면 API를 호출하여 새로운 데이터 페이지를 가져옴
        // buffer가 채워질 때까지 반복 호출
        while (buffer.isEmpty() && !isDone) {
            fetchNextPage();
        }

        // API 호출 후에도 버퍼가 비어있다면 끝난 것으로 간주하고 null, 있다면 하나를 꺼내 반환
        return buffer.isEmpty() ? null : buffer.poll();
    }

    //실제로 카카오 API를 호출하여 데이터를 가져오는 핵심 로직입니다.
    private void fetchNextPage() {
        // 모든 좌표(시청~건대)를 다 돌았다면 종료 처리
        if (gridIndex >= coordGrid.size()) {
            isDone = true;
            return;
        }

        // 현재 조사 중인 좌표 선택
        double[] coord = coordGrid.get(gridIndex);
        log.info("카카오 API 호출 시작 - gridIndex: {}, page: {}", gridIndex, currentPage);

        // 카카오 API 요청 URL 조립 (키워드 검색 API)
        String url = UriComponentsBuilder
                .fromUri(URI.create("https://dapi.kakao.com/v2/local/search/keyword.json"))
                .queryParam("query", "카페")              // 검색어
                .queryParam("category_group_code", "CE7") // 카페 카테고리 필터
                .queryParam("x", coord[0])                // 경도
                .queryParam("y", coord[1])                // 위도
                .queryParam("radius", 2000)               // 반경 2km 이내
                .queryParam("page", currentPage)          // 현재 페이지 번호
                .queryParam("size", 15)                   // 한 페이지에 담을 개수
                .toUriString();


        // 인증 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        // API 호출 수행
        ResponseEntity<KakaoSearchResponse> responseEntity = restTemplate.exchange(
                URI.create(url), HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoSearchResponse.class
        );

        KakaoSearchResponse response = responseEntity.getBody();

        // 데이터가 없거나, 마지막 페이지(isEnd)에 도달했다면
        if (response == null || response.documents().isEmpty() || response.meta().isEnd()) {
            gridIndex++;      // 다음 좌표 지점으로 이동
            currentPage = 1;  // 페이지 번호는 다시 1페이지부터 시작
        } else {
            // 가져온 데이터들을 버퍼에 모두 저장
            buffer.addAll(response.documents());
            // 다음 조사를 위해 페이지 번호 1 증가
            currentPage++;
        }
    }
}