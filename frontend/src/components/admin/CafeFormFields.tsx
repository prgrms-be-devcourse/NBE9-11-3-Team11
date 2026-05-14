'use client';

import { CafeType, FranchiseType, FloorCount, CongestionLevel } from '@/types/admin';

// 폼에서 쓰는 데이터 묶음 (Create/Edit 둘 다 이걸 씀)
export interface CafeFormData {
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    phone: string;
    description: string;
    type: CafeType;
    franchise: FranchiseType;
    hasToilet: boolean;
    hasOutlet: boolean;
    hasWifi: boolean;
    hasSeparateSpace: boolean;
    floorCount: FloorCount;
    congestionLevel: CongestionLevel;
    imageUrl: string;
}

// 카페 등록(Create) 할 때 처음 시작값 (빈 값들)
export const defaultFormData: CafeFormData = {
    name: '',
    address: '',
    latitude: 0,
    longitude: 0,
    phone: '',
    description: '',
    type: 'INDIVIDUAL',
    franchise: 'NONE',
    hasToilet: false,
    hasOutlet: false,
    hasWifi: false,
    hasSeparateSpace: false,
    floorCount: 'ONE',
    congestionLevel: 'LOW',
    imageUrl: '',
};

interface Props {
    formData: CafeFormData;                      // 현재 입력된 값들
    onChange: (updated: Partial<CafeFormData>) => void; // 값이 바뀔 때 호출되는 함수
}

export default function CafeFormFields({ formData, onChange }: Props) {

    // 주소 검색 버튼 눌렀을 때 실행
    const handleAddressSearch = () => {
        new window.daum.Postcode({
            oncomplete: async (data: any) => {
                const selectedAddress = data.roadAddress || data.jibunAddress;

                const res = await fetch(`/api/search?query=${encodeURIComponent(selectedAddress)}`);
                const result = await res.json();
                const doc = result.documents?.[0];

                // onChange 로 주소/위도/경도 한번에 업데이트
                onChange({
                    address: selectedAddress,
                    latitude: doc ? parseFloat(doc.y) : 0,
                    longitude: doc ? parseFloat(doc.x) : 0,
                });
            },
        }).open();
    };

    return (
        <div className="flex flex-col gap-3">

            {/* 카페 이름 */}
            <div>
                <label className="text-sm font-medium">카페 이름 *</label>
                <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => onChange({ name: e.target.value })}
                    placeholder="OO 카페"
                    className="w-full mt-1 p-2 border rounded"
                />
            </div>

            {/* 주소 */}
            <div>
                <label className="text-sm font-medium">주소 *</label>
                <div className="flex gap-2 mt-1">
                    <input
                        type="text"
                        value={formData.address}
                        readOnly
                        placeholder="주소를 검색하세요"
                        className="flex-1 p-2 border rounded bg-gray-50"
                    />
                    <button
                        onClick={handleAddressSearch}
                        className="px-3 py-2 bg-gray-800 text-white text-sm rounded hover:bg-gray-900"
                    >
                        검색
                    </button>
                </div>
            </div>

            {/* 전화번호 */}
            <div>
                <label className="text-sm font-medium">전화번호 *</label>
                <input
                    type="text"
                    value={formData.phone}
                    onChange={(e) => onChange({ phone: e.target.value })}
                    placeholder="02-0000-0000"
                    className="w-full mt-1 p-2 border rounded"
                />
            </div>

            {/* 설명 */}
            <div>
                <label className="text-sm font-medium">설명</label>
                <input
                    type="text"
                    value={formData.description}
                    onChange={(e) => onChange({ description: e.target.value })}
                    placeholder="조용한 카페"
                    className="w-full mt-1 p-2 border rounded"
                />
            </div>

            {/* 카페 종류 */}
            <div>
                <label className="text-sm font-medium">카페 종류 *</label>
                <select
                    value={formData.type}
                    onChange={(e) => onChange({ type: e.target.value as CafeType })}
                    className="w-full mt-1 p-2 border rounded"
                >
                    <option value="INDIVIDUAL">개인 카페</option>
                    <option value="FRANCHISE">프랜차이즈</option>
                </select>
            </div>

            {/* 프랜차이즈 종류 (FRANCHISE 선택 시에만 표시) */}
            {formData.type === 'FRANCHISE' && (
                <div>
                    <label className="text-sm font-medium">프랜차이즈 *</label>
                    <select
                        value={formData.franchise}
                        onChange={(e) => onChange({ franchise: e.target.value as FranchiseType })}
                        className="w-full mt-1 p-2 border rounded"
                    >
                        <option value="NONE">해당 없음</option>
                        <option value="STARBUCKS">스타벅스</option>
                        <option value="MEGA_COFFEE">메가커피</option>
                        <option value="EDIYA">이디야</option>
                        <option value="COMPOSE">컴포즈</option>
                        <option value="TWOSOME">투썸</option>
                        <option value="PAIK_DABANG">빽다방</option>
                        <option value="THE_VENTI">더벤티</option>
                    </select>
                </div>
            )}

            {/* 층수 */}
            <div>
                <label className="text-sm font-medium">층수 *</label>
                <select
                    value={formData.floorCount}
                    onChange={(e) => onChange({ floorCount: e.target.value as FloorCount })}
                    className="w-full mt-1 p-2 border rounded"
                >
                    <option value="ONE">1층</option>
                    <option value="TWO">2층</option>
                    <option value="THREE_OR_MORE">3층 이상</option>
                </select>
            </div>

            {/* 혼잡도 */}
            <div>
                <label className="text-sm font-medium">혼잡도 *</label>
                <select
                    value={formData.congestionLevel}
                    onChange={(e) => onChange({ congestionLevel: e.target.value as CongestionLevel })}
                    className="w-full mt-1 p-2 border rounded"
                >
                    <option value="LOW">여유</option>
                    <option value="MEDIUM">보통</option>
                    <option value="HIGH">혼잡</option>
                </select>
            </div>

            {/* 화장실 */}
            <div className="flex items-center justify-between">
                <label className="text-sm font-medium">화장실 유무 *</label>
                <input
                    type="checkbox"
                    checked={formData.hasToilet}
                    onChange={(e) => onChange({ hasToilet: e.target.checked })}
                    className="w-4 h-4"
                />
            </div>

            {/* 콘센트 */}
            <div className="flex items-center justify-between">
                <label className="text-sm font-medium">콘센트 유무 *</label>
                <input
                    type="checkbox"
                    checked={formData.hasOutlet}
                    onChange={(e) => onChange({ hasOutlet: e.target.checked })}
                    className="w-4 h-4"
                />
            </div>

            {/* 와이파이 */}
            <div className="flex items-center justify-between">
                <label className="text-sm font-medium">와이파이 유무 *</label>
                <input
                    type="checkbox"
                    checked={formData.hasWifi}
                    onChange={(e) => onChange({ hasWifi: e.target.checked })}
                    className="w-4 h-4"
                />
            </div>

            {/* 독립 공간 */}
            <div className="flex items-center justify-between">
                <label className="text-sm font-medium">공부 공간 유무 *</label>
                <input
                    type="checkbox"
                    checked={formData.hasSeparateSpace}
                    onChange={(e) => onChange({ hasSeparateSpace: e.target.checked })}
                    className="w-4 h-4"
                />
            </div>

            {/* 이미지 주소 */}
            <div>
                <label className="text-sm font-medium">이미지 주소</label>
                <input
                    type="text"
                    value={formData.imageUrl}
                    onChange={(e) => onChange({ imageUrl: e.target.value })}
                    className="w-full mt-1 p-2 border rounded"
                />
                {formData.imageUrl && (
                    <img
                        src={formData.imageUrl}
                        alt="이미지 미리보기"
                        className="mt-2 w-full rounded border"
                        onError={(e) => { e.currentTarget.style.display = 'none'; }}
                    />
                )}
            </div>

        </div>
    );
}