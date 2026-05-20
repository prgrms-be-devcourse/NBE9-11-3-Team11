# NBE9-11-3-Team11
백엔드 11기 3차 11팀 프로젝트 - 열일하조

# ☕ 카공데이 - 지도 기반 카페 탐색 서비스

> 카카오맵 기반으로 카공하기 좋은 카페를 탐색하고, 찜·리뷰·제보 기능과 관리자 운영 기능을 제공하는 웹 서비스
 


## 📌 프로젝트 소개


카페를 학습 공간으로 활용하는 **카공족**이 늘어나고 있지만, 기존 카페 서비스는 위치·메뉴 등 기본 정보 중심으로 제공되어 공부나 작업에 적합한 카페인지 방문 전에 판단하기 어려웠습니다.

카공데이는 이러한 불편함을 해결하기 위해, 혼잡도·콘센트·분리된 공간 등 **카공 환경 정보를 지도 기반으로 직관적으로 제공**하고사용자가 직접 카페를 제보해 **최신 정보를 함께 만들어 나가는 서비스**입니다.




## 💡 주요 기능

### 로그인
- 카카오 OAuth2 기반 소셜 로그인
- JWT 발급 및 HttpOnly Cookie 기반 인증 상태 유지
- Access Token / Refresh Token 관리
- 관리자 권한에 따른 페이지 접근 제어

### 지도
- 카카오맵 SDK 기반 메인 화면 제공
- 지도 마커 및 클러스터러 표시
- 카페 상세 패널 연동
- 검색 및 필터 기능 제공

### 카페
- 카페 목록 조회 / 상세 조회
- 카페 등록 / 제보
- 카페 삭제 및 승인 대기 관리
- 공통 DTO 구조 적용 (`CafeBaseInfo`, `CafeRequest`)

### 찜 & 리뷰
- 카페 찜 추가 / 취소 / 목록 조회
- 리뷰 생성 / 조회 / 수정 / 삭제
- 작성자 본인 또는 관리자만 수정/삭제 가능
- 페이징 처리 기반 조회 최적화

### 관리자
- 관리자 로그인
- 관리자 전용 페이지 접근 제어
- 카페 등록 / 삭제 / 승인 관리
- 관리자 API 호출 공통화



## 👥 팀원 소개

| 이름  | 담당                                                      |
|-----|---------------------------------------------------------|
|      | 공동 작업 - 백엔드 Kotlin 마이그레이션    |
| 최동현 | 팀장, 백엔드 - 관리자 카페 정보 CRUD 및 유저 제보, 모니터링 및 부하테스트 담당 <br/>프론트 - 필터링 모달      |
| 서준우 | 백엔드 - 카페 리뷰 CRUD 테스트 데이더 구현 및 Postgre 이전 담당 <br/>프론트 - 로그인 페이지, 리뷰 수정              |
| 안수빈 | 백엔드 - JWT 기반 사용자 인증/인가 및 토큰 관리 구현, aws 배포 <br/>프론트 - 관리자 페이지 구현 |
| 이형진 | 백엔드 - 사용자 OAuth, 관리자 로그인 구현, redis 도입 <br/>프론트 - 관리자 로그아웃 구현      |
| 최민규 | 백엔드 - 카페 목록/단건 검색, 찜 기능 구현, CI/CD, docker compose 작성 <br/>프론트 - 맵 연동 및 기본 화면 틀 구현 |



## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-25-007396?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=flat&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white)

### Frontend
![Next.js](https://img.shields.io/badge/Next.js-000000?style=flat&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat&logo=typescript&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat&logo=tailwindcss&logoColor=white)
![Zustand](https://img.shields.io/badge/Zustand-443E38?style=flat&logoColor=white)

### External API / Infra
![Kakao Map](https://img.shields.io/badge/Kakao_Map-FFCD00?style=flat&logoColor=black)
![Kakao OAuth](https://img.shields.io/badge/Kakao_OAuth-FFCD00?style=flat&logoColor=black)



## 📁 프로젝트 구조
### 백엔드
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/back/team11/
│   │   │   ├── BackendApplication.kt
│   │   │   ├── DataInitializer.kt
│   │   │   ├── domain/
│   │   │   │   ├── member/
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── Member.kt                      # Entity
│   │   │   │   │   │   ├── MemberRole.kt                  # Enum - ADMIN / USER
│   │   │   │   │   │   └── Provider.kt                    # Enum - LOCAL / KAKAO
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── MemberRepository.kt            # Repo
│   │   │   │   │   ├── service/
│   │   │   │   │   │   └── MemberService.kt               # Service
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   └── MemberController.kt            # Controller
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── MemberRequestDto.kt            # DTO
│   │   │   │   │       └── MemberResponseDto.kt           # DTO
│   │   │   │   │
│   │   │   │   ├── cafe/
│   │   │   │   │   ├── batch/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   ├── KakaoPlaceDto.kt
│   │   │   │   │   │   │   └── KakaoSearchResponse.kt
│   │   │   │   │   │   ├── CafeApiClient.kt               # Batch
│   │   │   │   │   │   ├── CafeCollectJobConfig.kt        # Batch
│   │   │   │   │   │   └── CafeItemProcessor.kt           # Batch
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── Cafe.kt                        # Entity
│   │   │   │   │   │   ├── CafeType.kt                    # Enum - FRANCHISE / INDIVIDUAL
│   │   │   │   │   │   ├── Franchise.kt                   # Enum - STARBUCKS / MEGA_COFFEE / NONE
│   │   │   │   │   │   ├── CafeStatus.kt                  # Enum - PENDING / APPROVED / REJECTED
│   │   │   │   │   │   ├── FloorCount.kt                  # Enum - ONE / TWO / THREE_OR_MORE
│   │   │   │   │   │   └── CongestionLevel.kt             # Enum - LOW / MEDIUM / HIGH
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── CafeRepository.kt              # Repo
│   │   │   │   │   │   ├── CafeRepositoryCustom.kt        # Repo
│   │   │   │   │   │   ├── CafeRepositoryImpl.kt          # Repo
│   │   │   │   │   │   └── CafeSearchCondition.kt         # Repo
│   │   │   │   │   ├── service/
│   │   │   │   │   │   ├── CafeService.kt                 # Service
│   │   │   │   │   │   └── AdminCafeService.kt            # Service
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── AdminCafeController.kt         # Controller
│   │   │   │   │   │   ├── CafeController.kt              # Controller
│   │   │   │   │   │   └── CafeSearchController.kt        # Controller
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── AdminCafeResponse.kt           # DTO
│   │   │   │   │       ├── AdminCafeSearchCondition.kt    # DTO
│   │   │   │   │       ├── CafeBaseInfo.kt                # DTO
│   │   │   │   │       ├── CafeDetailResponse.kt          # DTO
│   │   │   │   │       ├── CafeListResponse.kt            # DTO
│   │   │   │   │       ├── CafeRequest.kt                 # DTO
│   │   │   │   │       ├── CafeResponse.kt                # DTO
│   │   │   │   │       ├── CafeUpdateRequest.kt           # DTO
│   │   │   │   │       └── PageResponse.kt                # DTO
│   │   │   │   │
│   │   │   │   ├── review/
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   └── Review.kt                      # Entity
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── ReviewRepository.kt            # Repo
│   │   │   │   │   ├── service/
│   │   │   │   │   │   └── ReviewService.kt               # Service
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   └── ReviewController.kt            # Controller
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── ReviewRequestDto.kt            # DTO
│   │   │   │   │       └── ReviewResponseDto.kt           # DTO
│   │   │   │   │
│   │   │   │   └── wishlist/
│   │   │   │       ├── entity/
│   │   │   │       │   └── Wishlist.kt                    # Entity
│   │   │   │       ├── repository/
│   │   │   │       │   └── WishlistRepository.kt          # Repo
│   │   │   │       ├── service/
│   │   │   │       │   └── WishlistService.kt             # Service
│   │   │   │       ├── controller/
│   │   │   │       │   └── WishlistController.kt          # Controller
│   │   │   │       └── dto/
│   │   │   │           └── WishlistResponse.kt            # DTO
│   │   │   │
│   │   │   ├── auth/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── LoginRequestDto.kt                 # DTO - 관리자 로컬 로그인
│   │   │   │   │   └── TokenResponseDto.kt                # DTO - JWT 토큰
│   │   │   │   ├── oauth/
│   │   │   │   │   ├── CustomOAuth2UserService.kt         # Service
│   │   │   │   │   ├── OAuth2SuccessHandler.kt
│   │   │   │   │   └── OAuthAttributes.kt
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AdminAuthController.kt
│   │   │   │   │   ├── AuthController.kt
│   │   │   │   │   └── TokenReissueController.kt          # Controller
│   │   │   │   └── service/
│   │   │   │       ├── AuthService.kt
│   │   │   │       ├── TokenReissueService.kt
│   │   │   │       └── TokenService.kt                    # Service
│   │   │   │
│   │   │   └── global/
│   │   │       ├── config/
│   │   │       │   ├── QueryDslConfig.kt                  # Config
│   │   │       │   ├── RedisConfig.kt                     # Config - Redis 설정
│   │   │       │   ├── RestTemplateConfig.kt              # Config
│   │   │       │   └── SwaggerConfig.kt                   # Config
│   │   │       ├── dto/
│   │   │       │   └── PageResponse.kt                    # DTO
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.kt
│   │   │       │   ├── ErrorCode.kt
│   │   │       │   └── CustomException.kt
│   │   │       ├── extension/
│   │   │       │   └── RepositoryExtensions.kt
│   │   │       ├── rsData/
│   │   │       │   └── RsData.kt                          # 공통 응답 래퍼
│   │   │       ├── security/
│   │   │       │   ├── SecurityConfig.kt                  # Config
│   │   │       │   ├── JwtTokenProvider.kt                # Security
│   │   │       │   └── JwtAuthenticationFilter.kt         # Security
│   │   │       └── util/
│   │   │           ├── AuthUtil.kt
│   │   │           └── CookieUtil.kt
│   │   │
│   │   └── resources/
│   │       ├── application.yml                            # 공통
│   │       └── application-prod.yml                      # 배포 환경
│   │
│   └── test/java/com/back/team11/
│       ├── domain/
│       │   ├── auth/controller/
│       │   │   ├── AdminAuthControllerTest.kt
│       │   │   ├── AuthControllerTest.kt
│       │   │   └── TokenReissueControllerTest.kt
│       │   ├── cafe/controller/
│       │   │   ├── AdminCafeControllerTest.kt
│       │   │   ├── CafeControllerTest.kt
│       │   │   └── CafeSearchControllerTest.kt
│       │   ├── review/controller/
│       │   │   └── ReviewControllerTest.kt
│       │   └── wishlist/controller/
│       │       └── WishlistControllerTest.kt
│       └── BackendApplicationTests.kt
│
├── build.gradle.kts
└── settings.gradle.kts
```

### 프론트엔드
```
frontend/
├── .next/
├── node_modules/
├── public/
├── src/
│   ├── app/
│   │   ├── api/
│   │   │   ├── search/
│   │   │   │   └── route.ts
│   │   │   ├── auth.ts
│   │   │   └── client.ts
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── login-success/
│   │   │   └── page.tsx
│   │   ├── main/
│   │   │   ├── admin/
│   │   │   │   ├── cafe/
│   │   │   │   │   └── page.tsx
│   │   │   │   ├── login/
│   │   │   │   │   └── page.tsx
│   │   │   │   ├── pending/
│   │   │   │   │   └── page.tsx
│   │   │   │   └── rejected/
│   │   │   │       └── page.tsx
│   │   │   ├── cafe/
│   │   │   │   └── [id]/
│   │   │   │       └── page.tsx
│   │   │   ├── wishlist/
│   │   │   │   └── page.tsx
│   │   │   └── page.tsx
│   │   ├── oauth/
│   │   │   └── callback/
│   │   │       └── page.tsx
│   │   ├── favicon.ico
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   └── page.tsx
│   │
│   ├── components/
│   │   ├── admin/
│   │   │   ├── CafeCreateModal.tsx
│   │   │   ├── CafeDetailModal.tsx
│   │   │   ├── CafeEditModal.tsx
│   │   │   ├── CafeFormFields.tsx
│   │   │   ├── CafeList.tsx
│   │   │   ├── Logoutbutton.tsx
│   │   │   ├── page.tsx
│   │   │   ├── PaginationButtons.tsx
│   │   │   ├── PendingList.tsx
│   │   │   └── RejectedList.tsx
│   │   ├── cafe/
│   │   │   ├── CafeDetail.tsx
│   │   │   ├── page.tsx
│   │   │   ├── PopularCafeList.tsx
│   │   │   ├── ReportModal.tsx
│   │   │   └── WishlistPanel.tsx
│   │   ├── common/
│   │   │   ├── FilterModal.tsx
│   │   │   ├── Header.tsx
│   │   │   ├── page.tsx
│   │   │   └── SearchModal.tsx
│   │   ├── map/
│   │   │   ├── Map.tsx
│   │   │   └── page.tsx
│   │   └── review/
│   │       └── page.tsx
│   │
│   ├── hooks/
│   │   └── page.tsx
│   │
│   ├── lib/
│   │   └── api/
│   │       ├── admin.ts
│   │       ├── auth.ts
│   │       └── cafe.ts
│   │
│   ├── store/
│   │   └── authStore.ts
│   │
│   ├── types/
│   │   ├── admin.ts
│   │   ├── cafe.ts
│   │   └── kakao.d.ts
│   │
│   └── proxy.ts
│
├── .env.local
├── .gitignore
├── eslint.config.mjs
├── next-env.d.ts
├── next.config.ts
├── package-lock.json
├── package.json
├── postcss.config.mjs
├── README.md
├── tailwind.config.ts
└── tsconfig.json
```

🗄 ERD

![img.png](img.png)<br/>
2차 프로젝트 ERD에서 변경된 사항:
- `refresh_token` 테이블 제거 → Redis로 이전 (TTL 자동 만료 처리)

기존 테이블 구조 (`member`, `cafe`, `review`, `wishlist`) 동일하게 유지

## 🚀 실행 방법

### Backend
**1. 레포지토리 클론**
```bash
git clone https://github.com/prgrms-be-devcourse/NBE9-11-3-Team11.git
cd NBE9-11-3-Team11/backend
```

**2. 환경변수 설정**
```bash
CLIENT_ID={CLIENT_ID}
DB_USERNAME={DB_USERNAME}
DB_PASSWORD={DB_PASSWORD}
JWT_SECRET={YOUR_SECRET_JWT_KEY}
KAKAO_REST_API_KEY={KAKAO_REST_API_KEY}
REDIS_PASSWORD={REDIS_PASSWORD}
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cafe_study
REDIS_HOST=localhost
REDIS_PORT=6379
```

**3. 서버 실행**
```bash
./gradlew bootRun
```

### Frontend

**1. 패키지 설치**
```bash
cd frontend
npm install
```

**2. 환경변수 설정**
```bash
NEXT_PUBLIC_KAKAO_MAP_KEY={NEXT_PUBLIC_KAKAO_MAP_KEY}
KAKAO_REST_API_KEY={KAKAO_REST_API_KEY}
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

**3. 개발 서버 실행**
```bash
npm run dev
```
