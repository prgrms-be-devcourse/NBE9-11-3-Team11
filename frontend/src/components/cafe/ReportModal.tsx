"use client";

import { useState } from "react";
import { X } from "lucide-react";
import { reportCafe } from "@/lib/api/cafe";
import { CafeRequest } from "@/types/cafe";

interface ReportModalProps {
    onClose: () => void;
}

export default function ReportModal({ onClose }: ReportModalProps) {
    const [form, setForm] = useState({
        name: "",
        address: "",
        latitude: 0,
        longitude: 0,
        type: "",
        franchise: "NONE",
        hasWifi: false,
        hasOutlet: false,
        hasToilet: false,
        hasSeparateSpace: false,
        floorCount: "",
        congestionLevel: "",
        phone: "",
        description: "",
    });

    const handleAddressSearch = () => {
        new window.daum.Postcode({
            oncomplete: async (data: any) => {
                const address = data.roadAddress || data.jibunAddress;

                // 주소로 정확한 좌표 변환
                const res = await fetch(`/api/search?query=${encodeURIComponent(address)}`);
                const result = await res.json();
                const doc = result.documents?.[0];

                setForm({
                    ...form,
                    address,
                    latitude: doc ? parseFloat(doc.y) : 0,
                    longitude: doc ? parseFloat(doc.x) : 0,
                });
            },
        }).open();
    };

    const handleSubmit = async () => {
        if (!form.name || !form.address || !form.type || !form.floorCount || !form.congestionLevel) {
            alert("필수 항목을 모두 입력해주세요.");
            return;
        }

        const request: CafeRequest = {
            name: form.name,
            address: form.address,
            latitude: form.latitude,
            longitude: form.longitude,
            phone: form.phone || undefined,
            description: form.description || undefined,
            type: form.type,
            franchise: form.franchise,
            hasToilet: form.hasToilet,
            hasOutlet: form.hasOutlet,
            hasWifi: form.hasWifi,
            floorCount: form.floorCount,
            hasSeparateSpace: form.hasSeparateSpace,
            congestionLevel: form.congestionLevel,
        };

        try {
            await reportCafe(request);
            alert("제보가 완료되었습니다!");
            onClose();
        } catch (e) {
            alert("제보 중 오류가 발생했습니다.");
            console.error(e);
        }
    };

    const chipClass = (selected: boolean) =>
        `px-4 py-2 rounded-2xl text-sm font-medium border transition-colors cursor-pointer
        ${selected
            ? "bg-gray-800 text-white border-gray-800"
            : "bg-white text-gray-600 border-gray-200 hover:border-gray-400"}`;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-sm"
            onClick={onClose}
        >
            <div
                className="w-[520px] max-h-[80vh] bg-white rounded-3xl shadow-xl overflow-hidden flex flex-col"
                onClick={(e) => e.stopPropagation()}
            >
                {/* 헤더 */}
                <div className="px-6 py-4 border-b flex items-center justify-between flex-shrink-0">
                    <div>
                        <h2 className="font-bold text-lg text-gray-900">카페 제보하기</h2>
                        <p className="text-xs text-gray-400 mt-0.5">카공하기 좋은 카페를 알려주세요</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
                    >
                        <X size={18} />
                    </button>
                </div>

                {/* 폼 */}
                <div className="flex-1 overflow-y-auto scrollbar-hide p-6 space-y-6">

                    {/* 카페 이름 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            카페 이름 <span className="text-red-400 text-xs">필수</span>
                        </label>
                        <input
                            type="text"
                            placeholder="카페 이름을 입력하세요"
                            value={form.name}
                            onChange={(e) => setForm({ ...form, name: e.target.value })}
                            className="w-full px-4 py-3 rounded-2xl border border-gray-200 text-sm text-gray-900 placeholder-gray-400 outline-none focus:border-gray-400 transition-colors"
                        />
                    </div>

                    {/* 주소 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            주소 <span className="text-red-400 text-xs">필수</span>
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="text"
                                placeholder="주소를 검색하세요"
                                value={form.address}
                                readOnly
                                className="flex-1 px-4 py-3 rounded-2xl border border-gray-200 text-sm text-gray-900 placeholder-gray-400 outline-none bg-gray-50"
                            />
                            <button
                                onClick={handleAddressSearch}
                                className="px-4 py-3 rounded-2xl bg-gray-800 text-white text-sm font-medium hover:bg-gray-900 transition-colors whitespace-nowrap"
                            >
                                검색
                            </button>
                        </div>
                    </div>

                    {/* 카페 유형 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            카페 유형 <span className="text-red-400 text-xs">필수</span>
                        </label>
                        <div className="flex gap-2">
                            {[
                                { label: "프랜차이즈", value: "FRANCHISE" },
                                { label: "개인카페", value: "INDIVIDUAL" },
                            ].map((item) => (
                                <button
                                    key={item.value}
                                    onClick={() => setForm({ ...form, type: item.value, franchise: "NONE" })}
                                    className={chipClass(form.type === item.value)}
                                >
                                    {item.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* 프랜차이즈 선택 */}
                    {form.type === "FRANCHISE" && (
                        <div>
                            <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                                프랜차이즈 <span className="text-red-400 text-xs">필수</span>
                            </label>
                            <div className="flex gap-2">
                                {[
                                    { label: "스타벅스", value: "STARBUCKS" },
                                    { label: "메가커피", value: "MEGA_COFFEE" },
                                    { label: "이디야", value: "EDIYA" },
                                    { label: "컴포즈", value: "COMPOSE" },
                                    { label: "투썸", value: "TWOSOME" },
                                    { label: "빽다방", value: "PAIK_DABANG" },
                                    { label: "더벤티", value: "THE_VENTI" },
                                ].map((item) => (
                                    <button
                                        key={item.value}
                                        onClick={() => setForm({ ...form, franchise: item.value })}
                                        className={chipClass(form.franchise === item.value)}
                                    >
                                        {item.label}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* 편의시설 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            편의시설 <span className="text-gray-400 text-xs">선택</span>
                        </label>
                        <div className="flex flex-wrap gap-2">
                            {[
                                { label: "📶 와이파이", key: "hasWifi" },
                                { label: "🔌 콘센트", key: "hasOutlet" },
                                { label: "🚻 화장실", key: "hasToilet" },
                                { label: "🪑 분리된 공간", key: "hasSeparateSpace" },
                            ].map((item) => (
                                <button
                                    key={item.key}
                                    onClick={() => setForm({ ...form, [item.key]: !form[item.key as keyof typeof form] })}
                                    className={chipClass(!!form[item.key as keyof typeof form])}
                                >
                                    {item.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* 층수 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            층수 <span className="text-red-400 text-xs">필수</span>
                        </label>
                        <div className="flex gap-2">
                            {[
                                { label: "1층", value: "ONE" },
                                { label: "2층", value: "TWO" },
                                { label: "3층 이상", value: "THREE_OR_MORE" },
                            ].map((item) => (
                                <button
                                    key={item.value}
                                    onClick={() => setForm({ ...form, floorCount: item.value })}
                                    className={chipClass(form.floorCount === item.value)}
                                >
                                    {item.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* 혼잡도 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            혼잡도 <span className="text-red-400 text-xs">필수</span>
                        </label>
                        <div className="flex gap-2">
                            {[
                                { label: "🟢 여유", value: "LOW" },
                                { label: "🟡 보통", value: "MEDIUM" },
                                { label: "🟣 혼잡", value: "HIGH" },
                            ].map((item) => (
                                <button
                                    key={item.value}
                                    onClick={() => setForm({ ...form, congestionLevel: item.value })}
                                    className={chipClass(form.congestionLevel === item.value)}
                                >
                                    {item.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* 전화번호 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            전화번호 <span className="text-gray-400 text-xs">선택</span>
                        </label>
                        <input
                            type="text"
                            placeholder="02-0000-0000"
                            value={form.phone}
                            onChange={(e) => setForm({ ...form, phone: e.target.value })}
                            className="w-full px-4 py-3 rounded-2xl border border-gray-200 text-sm text-gray-900 placeholder-gray-400 outline-none focus:border-gray-400 transition-colors"
                        />
                    </div>

                    {/* 한 줄 설명 */}
                    <div>
                        <label className="text-sm font-semibold text-gray-700 mb-1.5 flex items-center gap-1">
                            한 줄 설명 <span className="text-gray-400 text-xs">선택</span>
                        </label>
                        <textarea
                            placeholder="카페에 대한 간단한 설명을 입력하세요"
                            value={form.description}
                            onChange={(e) => setForm({ ...form, description: e.target.value })}
                            rows={3}
                            className="w-full px-4 py-3 rounded-2xl border border-gray-200 text-sm text-gray-900 placeholder-gray-400 outline-none focus:border-gray-400 transition-colors resize-none"
                        />
                    </div>
                </div>

                {/* 하단 버튼 */}
                <div className="px-6 py-4 border-t flex-shrink-0">
                    <button
                        onClick={() => handleSubmit()}
                        className="w-full py-3.5 rounded-2xl bg-gray-800 text-white text-sm font-medium hover:bg-gray-900 transition-colors"
                    >
                        제보하기
                    </button>
                </div>
            </div>
        </div>
    );
}