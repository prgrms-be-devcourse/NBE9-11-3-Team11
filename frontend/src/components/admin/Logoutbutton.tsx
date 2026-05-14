'use client';

import { logoutAdmin } from '@/lib/api/auth'; // 로그아웃 로직 파일 경로 확인

export default function LogoutButton() {
    const handleLogout = () => {
        // 얼럿창도 조금 더 부드러운 문구로 변경
        if (confirm('정말 로그아웃 하시겠습니까?')) {
            logoutAdmin();
        }
    };

    return (
        <button
            onClick={handleLogout}
            // 기존 빨간색 제거, 무채색 톤 + 미니멀한 스타일 적용
            className="
                px-5 py-2.5 
                text-sm font-medium text-gray-700 
                bg-white border border-gray-200 
                rounded-full 
                shadow-sm 
                hover:bg-gray-50 hover:border-gray-300 hover:text-gray-900
                transition-all duration-200 ease-in-out
                flex items-center justify-center gap-2
            "
        >
            {/* 감성을 위한 심플한 로그아웃 아이콘 추가 (SVG) */}
            <svg 
                xmlns="http://www.w3.org/2000/svg" 
                fill="none" 
                viewBox="0 0 24 24" 
                strokeWidth={1.5} 
                stroke="currentColor" 
                className="w-4.5 h-4.5"
            >
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75" />
            </svg>
            로그아웃
        </button>
    );
}