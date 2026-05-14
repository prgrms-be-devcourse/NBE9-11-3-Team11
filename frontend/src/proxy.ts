import { NextRequest, NextResponse } from 'next/server';

// proxy.ts — Next.js 16에서 middleware.ts를 대체하는 파일 (Node.js 런타임에서 실행)
export function proxy(request: NextRequest) {
    const token = request.cookies.get('accessToken')?.value;

    // accessToken 쿠키가 없으면 관리자 로그인 페이지로 redirect
    if (!token) {
        return NextResponse.redirect(new URL('/main/admin/login', request.url));
    }

    try {
        // JWT 페이로드(중간 부분) base64url 디코딩 — 서명 검증은 백엔드에서 담당
        const payload = JSON.parse(
            Buffer.from(token.split('.')[1], 'base64url').toString()
        );

        // role 클레임이 ADMIN이 아니면 관리자 로그인 페이지로 redirect
        if (payload.role !== 'ADMIN') {
            return NextResponse.redirect(new URL('/main/admin/login', request.url));
        }
    } catch {
        // 토큰 파싱 실패 시(변조, 형식 오류 등) 관리자 로그인 페이지로 redirect
        return NextResponse.redirect(new URL('/main/admin/login', request.url));
    }

    return NextResponse.next();
}

// 관리자 로그인 페이지 자체는 제외하고 나머지 관리자 경로 전체에 적용
export const config = {
    matcher: [
        '/main/admin/cafe/:path*',
        '/main/admin/pending/:path*',
        '/main/admin/rejected/:path*',
    ],
};
