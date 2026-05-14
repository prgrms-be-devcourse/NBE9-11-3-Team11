'use client';

import { AdminCafe } from '@/types/admin';

interface Props {
    cafe: AdminCafe;           // 상세 보기할 카페 데이터
    onClose: () => void;       // 모달 닫기
}

export default function CafeDetailModal({ cafe: cafeData, onClose }: Props) {
    const { cafe } = cafeData;
    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto border-2 border-gray-300 shadow-xl">

                {/* 헤더 */}
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-bold">카페 상세 정보</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-black">✕</button>
                </div>

                {/* 이미지 */}
                {cafe.imageUrl && (
                    <img src={cafe.imageUrl} alt="카페 이미지" className="w-full rounded mb-4" />
                )}

                {/* 상세 정보 */}
                <div className="flex flex-col gap-3 text-sm">
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">카페 이름</span>
                        <span className="font-medium">{cafe.name}</span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">주소</span>
                        <span className="text-right">{cafe.address}</span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">전화번호</span>
                        <span>{cafe.phone ?? '-'}</span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">설명</span>
                        <span>{cafe.description ?? '-'}</span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">카페 종류</span>
                        <span>{cafe.type === 'FRANCHISE' ? '프랜차이즈' : '개인 카페'}</span>
                    </div>
                    {cafe.type === 'FRANCHISE' && (
                        <div className="flex justify-between border-b pb-2">
                            <span className="text-gray-500">프랜차이즈</span>
                            <span>
                                {cafe.franchise === 'STARBUCKS' ? '스타벅스' :
                                    cafe.franchise === 'MEGA_COFFEE' ? '메가커피' :
                                    cafe.franchise === 'EDIYA' ? '이디야' :
                                    cafe.franchise === 'COMPOSE' ? '컴포즈' :
                                    cafe.franchise === 'TWOSOME' ? '투썸' :
                                    cafe.franchise === 'PAIK_DABANG' ? '빽다방' :
                                    cafe.franchise === 'THE_VENTI' ? '더벤티' : '-'}
                            </span>
                        </div>
                    )}
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">층수</span>
                        <span>
                            {cafe.floorCount === 'ONE' ? '1층' :
                                cafe.floorCount === 'TWO' ? '2층' : '3층 이상'}
                        </span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-500">혼잡도</span>
                        <span>
                            {cafe.congestionLevel === 'LOW' ? '여유' :
                                cafe.congestionLevel === 'MEDIUM' ? '보통' : '혼잡'}
                        </span>
                    </div>

                    {/* 편의시설 */}
                    <div className="mt-1">
                        <p className="text-gray-500 mb-2">편의시설</p>
                        <div className="flex gap-2 flex-wrap">
                            {cafe.hasToilet && <span className="px-2 py-1 bg-gray-100 rounded">🚻 화장실</span>}
                            {cafe.hasOutlet && <span className="px-2 py-1 bg-gray-100 rounded">🔌 콘센트</span>}
                            {cafe.hasWifi && <span className="px-2 py-1 bg-gray-100 rounded">📶 와이파이</span>}
                            {cafe.hasSeparateSpace && <span className="px-2 py-1 bg-gray-100 rounded">📚 공부 공간</span>}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}