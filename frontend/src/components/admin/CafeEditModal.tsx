'use client';

import { useState } from 'react';
import { AdminCafe, CafeUpdateRequest } from '@/types/admin';
import CafeFormFields, { CafeFormData } from '@/components/admin/CafeFormFields';

interface Props {
    cafe: AdminCafe;                              // 수정할 카페 기존 데이터
    onClose: () => void;                          // 모달 닫기
    onSubmit: (cafeId: number, data: CafeUpdateRequest) => void; // 수정하기 버튼 눌렀을 때
}

export default function CafeEditModal({ cafe: cafeData, onClose, onSubmit }: Props) {
    const { cafe } = cafeData;

    // 기존 카페 데이터로 초기값 설정
    // ?? '' = null이면 빈 문자열로 대체
    const [formData, setFormData] = useState<CafeFormData>({
        name: cafe.name,
        address: cafe.address,
        latitude: cafe.latitude,
        longitude: cafe.longitude,
        phone: cafe.phone ?? '',
        description: cafe.description ?? '',
        type: cafe.type,
        franchise: cafe.franchise,
        hasToilet: cafe.hasToilet,
        hasOutlet: cafe.hasOutlet,
        hasWifi: cafe.hasWifi,
        hasSeparateSpace: cafe.hasSeparateSpace,
        floorCount: cafe.floorCount,
        congestionLevel: cafe.congestionLevel,
        imageUrl: cafe.imageUrl ?? '',
    });

    // 바뀐 필드만 업데이트, 나머지는 그대로 유지
    const handleChange = (updated: Partial<CafeFormData>) => {
        setFormData((prev) => ({ ...prev, ...updated }));
    };

    const handleSubmit = () => {
        // 필수값 비어있으면 전송 막기
        if (!formData.name || !formData.address || !formData.phone) {
            alert('카페 이름, 주소, 전화번호는 필수입니다!');
            return;
        }
        // cafeId 랑 수정된 데이터 같이 전송
        // 선택값은 빈 문자열이면 undefined로 변환
        onSubmit(cafe.cafeId, {
            ...formData,
            description: formData.description || undefined,
            imageUrl: formData.imageUrl || undefined,
        });
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto border-2 border-gray-300 shadow-xl">

                {/* 모달 헤더 */}
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-bold">카페 수정</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-black">✕</button>
                </div>

                {/* 공통 폼 컴포넌트 — 입력값이 바뀔 때마다 handleChange 호출 */}
                <CafeFormFields formData={formData} onChange={handleChange} />

                {/* 수정하기 버튼 */}
                <button
                    onClick={handleSubmit}
                    className="w-full mt-6 p-3 bg-black text-white rounded hover:bg-gray-800"
                >
                    수정하기
                </button>

            </div>
        </div>
    );
}