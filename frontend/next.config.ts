import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  output: 'standalone',
  env: {
    NEXT_PUBLIC_KAKAO_MAP_KEY: process.env.NEXT_PUBLIC_KAKAO_MAP_KEY,
  },
  // 프록시 설정 - /api 요청을 백엔드(8080)로 전달
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        // /api/로 시작하는 모든 요청을 백엔드로 전달
        destination: `${process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'}/api/:path*`,
        // NEXT_PUBLIC_API_URL 환경변수가 있으면 그 값 사용, 없으면 localhost:8080 사용
        // 로컬  → .env.local의 NEXT_PUBLIC_API_URL=http://localhost:8080 사용
        // EC2   → ci.yml에서 주입한 NEXT_PUBLIC_API_URL 사용
        // rewrites는 Next.js 서버사이드에서 동작
        // 브라우저가 직접 백엔드로 요청하지 않고 Next.js 서버가 대신 요청해줌
        // → CORS 문제 없음
      },
    ];
  },
};

export default nextConfig;

