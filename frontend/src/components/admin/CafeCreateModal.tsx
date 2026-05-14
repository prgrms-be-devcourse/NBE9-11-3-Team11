'use client';

import { useState } from 'react'; // useState = 값이 바뀌는 데이터를 저장하는 도구
import { CafeRequest } from '@/types/cafe';
import CafeFormFields, { CafeFormData, defaultFormData } from '@/components/admin/CafeFormFields';

interface Props {
    onClose: () => void;                        // 모달 닫기 버튼 눌렀을 때
    onSubmit: (data: CafeRequest) => void; // 등록하기 버튼 눌렀을 때
}

export default function CafeCreateModal({ onClose, onSubmit }: Props) {

    // 폼 입력값 전체를 객체 하나로 관리 (기존 useState 15개 → 1개로 통합)
    const [formData, setFormData] = useState<CafeFormData>(defaultFormData);

    // 변경된 필드만 병합 업데이트 (Partial 사용)
    const handleChange = (updated: Partial<CafeFormData>) => {
        setFormData((prev) => ({ ...prev, ...updated }));
    };

    const handleSubmit = () => {
        if (!formData.name || !formData.address || !formData.phone) {
            alert('카페 이름, 주소, 전화번호는 필수입니다!');
            return;
        }
        // 선택값(description, imageUrl)은 빈 문자열이면 undefined로 변환해서 전송
        onSubmit({
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
                    <h2 className="text-lg font-bold">카페 등록</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-black">✕</button>
                </div>

                {/* 공통 폼 컴포넌트 — 입력값이 바뀔 때마다 handleChange 호출 */}
                <CafeFormFields formData={formData} onChange={handleChange} />

                {/* 등록하기 버튼 */}
                <button
                    onClick={handleSubmit}
                    className="w-full mt-6 p-3 bg-black text-white rounded hover:bg-gray-800"
                >
                    등록하기
                </button>

            </div>
        </div>
    );
}