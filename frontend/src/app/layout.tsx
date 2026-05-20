import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import type { Metadata } from "next";
import Script from "next/script";
// 전역 CSS 파일 import
// Tailwind CSS 설정 등 전체 페이지에 적용되는 스타일

// Geist 폰트 설정
const geistSans = Geist({
  variable: "--font-geist-sans", // CSS 변수명으로 등록 (var(--font-geist-sans)로 사용 가능)
  subsets: ["latin"],            // 라틴 문자만 로드 (용량 최적화)
});

// Geist Mono 폰트 설정
const geistMono = Geist_Mono({
  variable: "--font-geist-mono", // CSS 변수명으로 등록
  subsets: ["latin"],
});

// 페이지 메타데이터 설정
// 브라우저 탭, 검색엔진에 표시되는 정보
export const metadata: Metadata = {
  title: "카공데이",                    // 브라우저 탭에 표시되는 제목
  description: "카공하기 좋은 카페 찾기", // 검색엔진에 표시되는 설명
};

// RootLayout → 모든 페이지를 감싸는 최상위 레이아웃 컴포넌트
// children → 각 페이지의 실제 내용이 들어오는 자리
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode; // children은 React 컴포넌트 타입
}>) {
  return (
    // html 태그 설정
    // lang="ko" → 스크린리더, 검색엔진에게 한국어 페이지임을 알림
    // className → 폰트 CSS 변수 적용 + h-full(높이 100%) + antialiased(폰트 부드럽게)
    <html
      lang="ko"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      {/* body 태그 설정
          min-h-full → 최소 높이를 부모(html) 높이로 설정
          flex flex-col → 자식 요소를 세로로 배치 */}
      <body className="min-h-full flex flex-col">
        <Script
          src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
          strategy="beforeInteractive"
        />
        {/* children → 각 페이지 컴포넌트가 여기에 렌더링됨
            예) app/page.tsx의 내용이 여기에 표시 */}
        {children}
      </body>
    </html>
  );
}
