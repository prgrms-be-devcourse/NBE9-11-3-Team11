# 1단계: 빌드
# Node.js 이미지 기반으로 Next.js 앱 빌드
# 현재: Next.Next.js 16.2.4, Node.js 기반
FROM node:20-alpine AS builder
WORKDIR /app

# 의존성 파일 먼저 복사 (캐싱 최적화)
# 소스코드 변경 시 의존성 다운로드를 다시 하지 않도록
COPY package.json package-lock.json ./

#npm ci  → package-lock.json 기반으로 정확한 버전 설치
#npm install → package.json 기반으로 설치 (버전이 달라질 수 있음)
RUN npm ci

# NEXT_PUBLIC_ 변수는 빌드 타임에 번들에 포함되므로 ARG로 받아야 함
# CD에서 docker build --build-arg로 주입
# 런타임에 환경변수로 주입해도 적용 안 됨 (빌드 시점에 번들에 박히기 때문)
ARG NEXT_PUBLIC_KAKAO_MAP_KEY
ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_KAKAO_MAP_KEY=$NEXT_PUBLIC_KAKAO_MAP_KEY
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL

# 현재 로컬 디렉토리의 모든 파일을 컨테이너 현재 디렉토리로 복사
COPY . .
RUN npm run build


# 2단계: 실행
FROM node:20-alpine
WORKDIR /app

# Next.js standalone 모드 빌드 결과물만 복사
# standalone → 실행에 필요한 파일만 포함 (node_modules 불필요)
# static → CSS, JS 등 정적 파일
# public → 이미지 등 public 폴더
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

# 컨테이너 시작 시 node server.js 명령어를 실행
ENTRYPOINT ["node", "server.js"]