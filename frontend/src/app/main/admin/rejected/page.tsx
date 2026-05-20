'use client';

import { useState, useEffect } from 'react';
import { AdminCafe } from '@/types/admin';
import { fetchCafes } from '@/lib/api/admin';
import RejectedList from '@/components/admin/RejectedList';
import LogoutButton from '@/components/admin/Logoutbutton';
import PaginationButtons from '@/components/admin/PaginationButtons';

export default function AdminRejectedPage() {

    // 승인 거절 카페 목록
    const [cafes, setCafes] = useState<AdminCafe[]>([]);

    // 로딩 상태
    const [isLoading, setIsLoading] = useState(false);

    // 현재 페이지 번호 (1부터 시작)
    const [currentPage, setCurrentPage] = useState(1);
    // 전체 페이지 수
    const [totalPages, setTotalPages] = useState(0);


    // 승인 거절 목록 조회
    const loadRejectedCafes = async (page: number = 1) => {
        setIsLoading(true);
        try {
            const data = await fetchCafes('REJECTED', page);
            setCafes(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.log('승인 거절 목록 조회 실패:', error);
        } finally {
            setIsLoading(false);
        }
    };


    //  currentPage 변경 시 거절된 카페 목록 조회 (초기 렌더 포함)
    useEffect(() => {
        loadRejectedCafes(currentPage);
    }, [currentPage]);


    return (
        <div className="flex min-h-screen">

            {/* 왼쪽 사이드바 */}
            <div className="w-40 border-r p-4 flex flex-col gap-4">
                <span className="text-sm text-gray-500">관리자 페이지</span>
                <a href="/main/admin/cafe" className="text-gray-500">카페 목록</a>
                <a href="/main/admin/pending" className="text-gray-500">승인 대기</a>
                <a href="/main/admin/rejected" className="font-medium">승인 거절</a>

                {/* 하단으로 밀어내기 위한 여백 공간 */}
                <div className="flex-1"></div>

                {/* 사이드바 맨 하단에 로그아웃 버튼 추가 */}
                <div className="pt-4 border-t">
                    <LogoutButton />
                </div>
            </div>

            {/* 오른쪽 메인 영역 */}
            <div className="flex-1 p-6">

                {/* 헤더 */}
                <h1 className="text-xl font-bold mb-6">승인 거절</h1>

                {/* 로딩 중일 때 표시 */}
                {isLoading ? (
                    <p className="text-gray-500">불러오는 중...</p>
                ) : (
                    <RejectedList cafes={cafes} />
                )}

                <PaginationButtons
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={(page) => setCurrentPage(page)}
                />

            </div>

        </div>
    );
}