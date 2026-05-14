'use client';

interface Props {
    currentPage: number;   // 현재 페이지 번호
    totalPages: number;    // 전체 페이지 수
    onPageChange: (page: number) => void; // 페이지 변경 시 호출
}

export default function PaginationButtons({ currentPage, totalPages, onPageChange }: Props) {

    // 현재 페이지가 속한 그룹 계산 (10개씩)
    // 예) 1~10페이지 → groupStart=1, groupEnd=10
    //     11~20페이지 → groupStart=11, groupEnd=20
    const groupStart = Math.floor((currentPage - 1) / 10) * 10 + 1;
    const groupEnd = Math.min(groupStart + 9, totalPages);

    // 이전 그룹 첫 페이지 (groupStart - 10)
    const prevGroupPage = groupStart - 10;
    // 다음 그룹 첫 페이지 (groupStart + 10)
    const nextGroupPage = groupStart + 10;

    return (
        <div className="flex items-center justify-center gap-2 mt-6">
            {/* 이전 그룹 버튼 */}
            <button
                onClick={() => onPageChange(prevGroupPage)}
                disabled={groupStart === 1}
                className="w-8 h-8 flex items-center justify-center rounded disabled:opacity-30 hover:bg-gray-100"
            >
                &lt;
            </button>

            {/* 페이지 번호 (10개씩) */}
            {Array.from({ length: groupEnd - groupStart + 1 }, (_, i) => groupStart + i).map((page) => (
                <button
                    key={page}
                    onClick={() => onPageChange(page)}
                    className={`w-8 h-8 flex items-center justify-center rounded text-sm ${
                        currentPage === page ? 'bg-gray-200 font-bold' : 'hover:bg-gray-100'
                    }`}
                >
                    {page}
                </button>
            ))}

            {/* 다음 그룹 버튼 */}
            <button
                onClick={() => onPageChange(nextGroupPage)}
                disabled={groupEnd === totalPages}
                className="w-8 h-8 flex items-center justify-center rounded disabled:opacity-30 hover:bg-gray-100"
            >
                &gt;
            </button>
        </div>
    );
}