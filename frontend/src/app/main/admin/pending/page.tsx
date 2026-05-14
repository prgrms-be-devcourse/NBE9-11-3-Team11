'use client';

import { useState, useEffect } from 'react';
import { AdminCafe } from '@/types/admin';
import { fetchCafes, approveCafe, rejectCafe } from '@/lib/api/admin';
import PendingList from '@/components/admin/PendingList';
import LogoutButton from '@/components/admin/Logoutbutton';
import PaginationButtons from '@/components/admin/PaginationButtons';

export default function AdminPendingPage() {

    // 승인 대기 카페 목록
    const [cafes, setCafes] = useState<AdminCafe[]>([]);

    // 로딩 상태
    const [isLoading, setIsLoading] = useState(false);

    // 현재 페이지 번호 (1부터 시작)
    const [currentPage, setCurrentPage] = useState(1);
    // 전체 페이지 수
    const [totalPages, setTotalPages] = useState(0);


    // 승인 대기 목록 조회
    const loadPendingCafes = async (page: number = 1) => {
        setIsLoading(true);
        try {
            const data = await fetchCafes('PENDING', page);
            setCafes(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.log('승인 대기 목록 조회 실패:', error);
        } finally {
            setIsLoading(false);
        }
    };


    // 승인 처리
    const handleApprove = async (cafeId: number) => {
        if (!confirm('승인하시겠습니까?')) return;
        try {
            await approveCafe(cafeId);  // API 호출
            alert('카페 승인 성공');
            loadPendingCafes(); // 목록 새로고침
        } catch (error) {
            console.log('카페 승인 실패:', error);
            alert('카페 승인 실패');
        }
    };


    // 거절 처리
    const handleReject = async (cafeId: number) => {
        if (!confirm('거절하시겠습니까?')) return;
        try {
            await rejectCafe(cafeId);
            alert('카페 승인 거부 성공');
            loadPendingCafes();
        } catch (error) {
            console.log('카페 거절 실패:', error);
            alert('카페 승인 거부 실패');
        }
    };


    /// currentPage 변경 시 승인 대기 카페 목록 조회(초기 렌더 포함)
    useEffect(() => {
        loadPendingCafes(currentPage);
    }, [currentPage]);


    return (
        <div className="flex min-h-screen">

            {/* 왼쪽 사이드바 */}
            <div className="w-40 border-r p-4 flex flex-col gap-4">
                <span className="text-sm text-gray-500">관리자 페이지</span>
                <a href="/main/admin/cafe" className="text-gray-500">카페 목록</a>
                <a href="/main/admin/pending" className="font-medium">승인 대기</a>
                <a href="/main/admin/rejected" className="text-gray-500">승인 거절</a>

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
                <h1 className="text-xl font-bold mb-6">승인 대기</h1>

                {/* 로딩 중일 때 표시 */}
                {isLoading ? (
                    <p className="text-gray-500">불러오는 중...</p>
                ) : (
                    <PendingList
                        cafes={cafes}
                        onApprove={handleApprove}
                        onReject={handleReject}
                        onDetail={() => {}}
                    />
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