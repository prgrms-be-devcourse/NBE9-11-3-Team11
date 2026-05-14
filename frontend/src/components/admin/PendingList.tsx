'use client';

import { AdminCafe } from '@/types/admin';
import { useState } from 'react';

// PendingList 가 받는 데이터
interface Props {
    cafes: AdminCafe[];                    // 승인 대기 카페 목록
    onApprove: (cafeId: number) => void;   // 승인 버튼 눌렀을 때
    onReject: (cafeId: number) => void;    // 거절 버튼 눌렀을 때
    onDetail: (cafe: AdminCafe) => void;
}

export default function PendingList({ cafes, onApprove, onReject, onDetail }: Props) {
    const [openCafeId, setOpenCafeId] = useState<number | null>(null);
    return (
        <div className="w-full">

            {/* 카페 총 개수 */}
            <p className="mb-4 text-sm text-gray-500">
                총 {cafes.length}곳
            </p>

            {/* 승인 대기 목록 */}
            <div className="flex flex-col gap-2">
                {cafes.map((item) => {
                    const { cafe } = item;
                    return (
                        <div
                            key={cafe.cafeId}
                            className="flex flex-col border rounded-lg overflow-hidden"
                        >
                            {/* 카페 이름 + 버튼 감싸는 div 추가 */}
                            <div className="flex items-center justify-between p-4">

                                {/* 카페 이름 */}
                                <span
                                    className="font-medium cursor-pointer hover:underline"
                                    onClick={() => setOpenCafeId(openCafeId === cafe.cafeId ? null : cafe.cafeId)}
                                >
                                    {cafe.name}
                                </span>

                                {/* 승인 / 거절 버튼 */}
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => onApprove(cafe.cafeId)}
                                        className="px-3 py-1 text-sm border rounded text-blue-500 hover:bg-blue-50"
                                    >
                                        승인
                                    </button>
                                    <button
                                        onClick={() => onReject(cafe.cafeId)}
                                        className="px-3 py-1 text-sm border rounded text-red-500 hover:bg-red-50"
                                    >
                                        거절
                                    </button>
                                </div>
                            </div>
                            {openCafeId === cafe.cafeId && (
                                <div className="border-t p-4 bg-gray-50 flex flex-col gap-2 text-sm">
                                    {cafe.imageUrl && (
                                        <img src={cafe.imageUrl} alt="카페 이미지" className="w-full rounded mb-2" />
                                    )}
                                    <p><span className="font-medium">주소</span> : {cafe.address}</p>
                                    <p><span className="font-medium">전화번호</span> : {cafe.phone ?? '-'}</p>
                                    <p><span className="font-medium">설명</span> : {cafe.description ?? '-'}</p>
                                    <p><span className="font-medium">카페 종류</span> : {cafe.type === 'FRANCHISE' ? '프랜차이즈' : '개인 카페'}</p>
                                    {cafe.type === 'FRANCHISE' && (
                                        <p><span className="font-medium">프랜차이즈</span> : {
                                            cafe.franchise === 'STARBUCKS' ? '스타벅스' :
                                            cafe.franchise === 'MEGA_COFFEE' ? '메가커피' :
                                            cafe.franchise === 'EDIYA' ? '이디야' :
                                            cafe.franchise === 'COMPOSE' ? '컴포즈' :
                                            cafe.franchise === 'TWOSOME' ? '투썸' :
                                            cafe.franchise === 'PAIK_DABANG' ? '빽다방' :
                                            cafe.franchise === 'THE_VENTI' ? '더벤티' : '-'
                                        }</p>
                                    )}
                                    <p><span className="font-medium">층수</span> : {cafe.floorCount === 'ONE' ? '1층' : cafe.floorCount === 'TWO' ? '2층' : '3층 이상'}</p>
                                    <p><span className="font-medium">혼잡도</span> : {cafe.congestionLevel === 'LOW' ? '여유' : cafe.congestionLevel === 'MEDIUM' ? '보통' : '혼잡'}</p>
                                    <div className="flex gap-2 flex-wrap mt-1">
                                        {cafe.hasToilet && <span className="px-2 py-1 bg-gray-100 rounded">🚻 화장실</span>}
                                        {cafe.hasOutlet && <span className="px-2 py-1 bg-gray-100 rounded">🔌 콘센트</span>}
                                        {cafe.hasWifi && <span className="px-2 py-1 bg-gray-100 rounded">📶 와이파이</span>}
                                        {cafe.hasSeparateSpace && <span className="px-2 py-1 bg-gray-100 rounded">📚 공부 공간</span>}
                                    </div>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

        </div>
    );
}