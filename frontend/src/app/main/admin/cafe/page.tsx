'use client';

import { useState, useEffect} from 'react';
import { AdminCafe, CafeUpdateRequest } from '@/types/admin';
import { CafeRequest } from '@/types/cafe';
import { fetchCafes, createCafe, updateCafe, deleteCafe } from '@/lib/api/admin'; // API 함수 가져오기
import CafeList from '@/components/admin/CafeList';
import CafeCreateModal from '@/components/admin/CafeCreateModal';
import CafeEditModal from '@/components/admin/CafeEditModal';
import CafeDetailModal from '@/components/admin/CafeDetailModal';
import LogoutButton from '@/components/admin/Logoutbutton';
import PaginationButtons from '@/components/admin/PaginationButtons';

export default function AdminCafePage() {

    // 상태 관리
    // 카페 목록 데이터
    const [cafes, setCafes] = useState<AdminCafe[]>([]);

    // 등록 모달 열림/닫힘 여부 (true = 열림, false = 닫힘)
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    // 수정 모달에서 쓸 선택된 카페 (null = 선택 안 됨)
    const [selectedCafe, setSelectedCafe] = useState<AdminCafe | null>(null);

    // 로딩 상태
    const [isLoading, setIsLoading] = useState(false);

    // 상세 보기에서 쓸 선택된 카페 (null = 선택 안 됨)
    const [detailCafe, setDetailCafe] = useState<AdminCafe | null>(null);

    // 현재 페이지 번호 (1부터 시작)
    const [currentPage, setCurrentPage] = useState(1);
    // 전체 페이지 수
    const [totalPages, setTotalPages] = useState(0);

    // 전체 카페 수
    const [totalElements, setTotalElements] = useState(0);

    // 검색어 상태
    const [searchName, setSearchName] = useState('');


    // 카페 목록 조회
    const loadCafes = async (page: number = 1, name: string = '') => {
        setIsLoading(true);
        try {
            const data = await fetchCafes('APPROVED', page, name);
            setCafes(data.content);
            setTotalPages(data.totalPages); // 전체 페이지 수 저장
            setTotalElements(data.totalElements);
        } catch (error) {
            console.log('카페 목록 조회 실패:', error);
        } finally {
            setIsLoading(false);
        }
    };

    // 카페 등록 처리
    const handleCreate = async (data: CafeRequest) => {
        try {
            await createCafe(data); // API 호출
            alert('카페 등록 성공');
            setIsCreateModalOpen(false);
            loadCafes(); // 목록 새로고침
        } catch (error) {
            console.log('카페 등록 실패:', error);
            alert('카페 등록 실패');
        }
    };

    // 카페 수정 처리
    const handleEdit = async (cafeId: number, data: CafeUpdateRequest) => {
        try {
            await updateCafe(cafeId, data);
            alert('카페 정보 수정 성공');
            setSelectedCafe(null);
            loadCafes();
        } catch (error) {
            console.log('카페 수정 실패:', error);
            alert('카페 정보 수정 실패');
        }
    };


    // 카페 삭제 처리
    const handleDelete = async (cafeId: number) => {
        if (!confirm('정말 삭제할까요?')) return;
        try {
            await deleteCafe(cafeId);
            alert('카페 삭제 성공');
            loadCafes();
        } catch (error) {
            console.log('카페 삭제 실패:', error);
            alert('카페 삭제 실패');
        }
    };


    // 페이지 처음 열릴 때 목록 자동으로 불러오기 + 페이징
    useEffect(() => {
        loadCafes(currentPage, searchName);
    }, [currentPage]);


    return (
        <div className="flex min-h-screen">

            {/* 왼쪽 사이드바 */}
            <div className="w-40 border-r p-4 flex flex-col gap-4">
                <span className="text-sm text-gray-500">관리자 페이지</span>
                <a href="/main/admin/cafe" className="font-medium">카페 목록</a>
                <a href="/main/admin/pending" className="text-gray-500">승인 대기</a>
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
                <div className="flex items-center justify-between mb-6">
                    <h1 className="text-xl font-bold">카페 목록</h1>
                    {/* 등록하기 버튼 */}
                    <button
                        onClick={() => setIsCreateModalOpen(true)}
                        // setIsCreateModalOpen(true) = 등록 모달 열기
                        className="px-4 py-2 bg-black text-white rounded hover:bg-gray-800"
                    >
                        등록하기
                    </button>
                </div>

                {/* 검색창 */}
                <div className="flex gap-2 mb-4">
                    <input
                        type="text"
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                        placeholder="카페 이름 검색"
                        className="flex-1 p-2 border rounded"
                    />
                    <button
                        onClick={() => {
                            setCurrentPage(1);
                            loadCafes(1, searchName);
                        }}
                        className="px-4 py-2 bg-black text-white rounded hover:bg-gray-800"
                    >
                        검색
                    </button>
                </div>

                {/* 카페 목록 컴포넌트 */}
                <CafeList
                    cafes={cafes}
                    totalElements={totalElements}
                    onEdit={(cafe) => setSelectedCafe(cafe)}
                    onDelete={handleDelete}
                    onDetail={(cafe) => setDetailCafe(cafe)}
                />

                {/* 페이징 버튼 */}
                <PaginationButtons
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={(page) => setCurrentPage(page)}
                />

            </div>

            {/* 등록 모달 - isCreateModalOpen 이 true 일 때만 보임 */}
            {isCreateModalOpen && (
                <CafeCreateModal
                    onClose={() => setIsCreateModalOpen(false)}
                    onSubmit={handleCreate}
                />
            )}

            {/* 상세 모달 - detailCafe 가 있을 때만 보임 */}
            {detailCafe && (
                <CafeDetailModal
                    cafe={detailCafe}
                    onClose={() => setDetailCafe(null)}
                />
            )}

            {/* 수정 모달 - selectedCafe 가 있을 때만 보임 */}
            {selectedCafe && (
                <CafeEditModal
                    cafe={selectedCafe}
                    onClose={() => setSelectedCafe(null)}
                    onSubmit={handleEdit}
                />
            )}

        </div>
    );
}