'use client';

import { AdminCafe } from '@/types/admin';

// RejectedList 가 받는 데이터
interface Props {
    cafes: AdminCafe[]; // 승인 거절 카페 목록
}

export default function RejectedList({ cafes }: Props) {
    return (
        <div className="w-full">

            {/* 카페 총 개수 */}
            <p className="mb-4 text-sm text-gray-500">
                총 {cafes.length}곳
            </p>

            {/* 승인 거절 목록 */}
            <div className="flex flex-col gap-2">
                {cafes.map((item) => {
                    const { cafe } = item;
                    return (
                        <div
                            key={cafe.cafeId}
                            className="flex items-center justify-between p-4 border rounded-lg"
                        >
                            {/* 카페 이름 */}
                            <span className="font-medium">{cafe.name}</span>

                            {/* 거절 상태 표시 */}
                            <span className="px-3 py-1 text-sm text-red-500">
                                거절됨
                            </span>
                        </div>
                    );
                })}
            </div>

        </div>
    );
}