'use client';

// AdminCafe 타입 가져오기
import { AdminCafe } from '@/types/admin';

// CafeList 컴포넌트가 받는 데이터 형태 정의
interface Props {
    cafes: AdminCafe[];
    totalElements: number; // 전체 카페 수
    onEdit: (cafe: AdminCafe) => void;    // 수정 버튼 눌렀을 때 실행할 함수
    onDelete: (cafeId: number) => void;   // 삭제 버튼 눌렀을 때 실행할 함수
    onDetail: (cafe: AdminCafe) => void;  // 카페 클릭 시 상세 보기
}

export default function CafeList({ cafes, totalElements, onEdit, onDelete, onDetail }: Props) {
    return (
        <div className="w-full">

            {/* 카페 총 개수 표시 */}
            <p className="mb-4 text-sm text-gray-500">
                총 {totalElements}곳
            </p>

            {/* 카페 목록 */}
            <div className="flex flex-col gap-2">
                {cafes.map((item) => {
                    const { cafe } = item;
                    return (
                        // cafe.cafeId 를 key 로 사용
                        <div
                            key={cafe.cafeId}
                            className="flex items-center justify-between p-4 border rounded-lg"
                        >
                            {/* 카페 이름 */}
                            <span
                                className="font-medium cursor-pointer hover:underline"
                                onClick={() => onDetail(item)}
                            >
                                {cafe.name}
                            </span>

                            {/* 수정 / 삭제 버튼 */}
                            <div className="flex gap-2">
                                <button
                                    onClick={() => onEdit(item)}  // 수정 시 선택된 카페 전달
                                    className="px-3 py-1 text-sm border rounded hover:bg-gray-100"
                                >
                                    수정
                                </button>
                                <button
                                    onClick={() => onDelete(cafe.cafeId)} // 삭제는 ID만 필요
                                    className="px-3 py-1 text-sm border rounded text-red-500 hover:bg-red-50"
                                >
                                    삭제
                                </button>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}