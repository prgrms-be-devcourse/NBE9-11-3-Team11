"use client";

import { useState } from "react";
import { X } from "lucide-react";

export interface FilterState {
    type: string[];
    franchise: string[];
    hasWifi: boolean | null;
    hasOutlet: boolean | null;
    hasToilet: boolean | null;
    hasSeparateSpace: boolean | null;
    floorCount: string[];
    congestionLevel: string[];
}

interface FilterModalProps {
    onClose: () => void;
    onApply: (filters: FilterState) => void;
    currentFilters: FilterState;
}

const initialFilters: FilterState = {
    type: [],
    franchise: [],
    hasWifi: null,
    hasOutlet: null,
    hasToilet: null,
    hasSeparateSpace: null,
    floorCount: [],
    congestionLevel: [],
};

export default function FilterModal({ onClose, onApply, currentFilters }: FilterModalProps) {
    const [filters, setFilters] = useState<FilterState>(currentFilters);

    const toggleType = (value: string) => {
        setFilters((prev) => {
            const isSelected = prev.type.includes(value);
            if (isSelected) {
                return {
                    ...prev,
                    type: prev.type.filter((t) => t !== value),
                    franchise: value === "FRANCHISE" ? [] : prev.franchise,
                };
            }
            return { ...prev, type: [...prev.type, value] };
        });
    };

    const toggleBrand = (value: string) => {
        setFilters((prev) => {
            const newFranchise = prev.franchise.includes(value)
                ? prev.franchise.filter((f) => f !== value)
                : [...prev.franchise, value];

            return {
                ...prev,
                franchise: newFranchise,
                type: newFranchise.length === 0
                    ? prev.type.filter((t) => t !== "FRANCHISE")
                    : prev.type,
            };
        });
    };

    const toggleArray = (key: keyof Pick<FilterState, "floorCount" | "congestionLevel">, value: string) => {
        setFilters((prev) => ({
            ...prev,
            [key]: prev[key].includes(value)
                ? prev[key].filter((v) => v !== value)
                : [...prev[key], value],
        }));
    };

    const toggleBoolean = (key: keyof Pick<FilterState, "hasWifi" | "hasOutlet" | "hasToilet" | "hasSeparateSpace">) => {
        setFilters((prev) => ({
            ...prev,
            [key]: prev[key] === true ? null : true,
        }));
    };

    const chipClass = (selected: boolean) =>
        `px-4 py-2 rounded-2xl text-sm font-medium border transition-colors cursor-pointer
        ${selected
            ? "bg-gray-800 text-white border-gray-800"
            : "bg-white text-gray-600 border-gray-200 hover:border-gray-400"}`;

    return (
        <div
            className="fixed inset-0 z-50 flex items-end justify-center bg-black/20 backdrop-blur-sm"
            onClick={onClose}
        >
            <div
                className="w-full max-w-lg bg-white rounded-t-3xl shadow-xl p-6 space-y-6"
                onClick={(e) => e.stopPropagation()}
            >
                {/* 헤더 */}
                <div className="flex items-center justify-between">
                    <h2 className="font-bold text-lg text-gray-900">필터</h2>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
                    >
                        <X size={18} />
                    </button>
                </div>

                {/* 카페 유형 */}
                <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-3">카페 유형</h3>
                    <div className="flex flex-wrap gap-2">
                        <button
                            onClick={() => toggleType("FRANCHISE")}
                            className={chipClass(filters.type.includes("FRANCHISE"))}
                        >
                            프랜차이즈
                        </button>
                        <button
                            onClick={() => toggleType("INDIVIDUAL")}
                            className={chipClass(filters.type.includes("INDIVIDUAL"))}
                        >
                            개인카페
                        </button>
                    </div>

                    {filters.type.includes("FRANCHISE") && (
                        <div className="flex flex-wrap gap-2 mt-3 pl-1">
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
                                    onClick={() => toggleBrand(item.value)}
                                    className={chipClass(filters.franchise.includes(item.value))}
                                >
                                    {item.label}
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                {/* 편의시설 */}
                <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-3">편의시설</h3>
                    <div className="flex flex-wrap gap-2">
                        {[
                            { label: "📶 와이파이", key: "hasWifi" },
                            { label: "🔌 콘센트", key: "hasOutlet" },
                            { label: "🚻 화장실", key: "hasToilet" },
                            { label: "🪑 분리된 공간", key: "hasSeparateSpace" },
                        ].map((item) => (
                            <button
                                key={item.key}
                                onClick={() => toggleBoolean(item.key as keyof Pick<FilterState, "hasWifi" | "hasOutlet" | "hasToilet" | "hasSeparateSpace">)}
                                className={chipClass(filters[item.key as keyof FilterState] === true)}
                            >
                                {item.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* 층수 */}
                <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-3">층수</h3>
                    <div className="flex flex-wrap gap-2">
                        {[
                            { label: "1층", value: "ONE" },
                            { label: "2층", value: "TWO" },
                            { label: "3층 이상", value: "THREE_OR_MORE" },
                        ].map((item) => (
                            <button
                                key={item.value}
                                onClick={() => toggleArray("floorCount", item.value)}
                                className={chipClass(filters.floorCount.includes(item.value))}
                            >
                                {item.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* 혼잡도 */}
                <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-3">혼잡도</h3>
                    <div className="flex flex-wrap gap-2">
                        {[
                            { label: "🟢 여유", value: "LOW" },
                            { label: "🟡 보통", value: "MEDIUM" },
                            { label: "🟣 혼잡", value: "HIGH" },
                        ].map((item) => (
                            <button
                                key={item.value}
                                onClick={() => toggleArray("congestionLevel", item.value)}
                                className={chipClass(filters.congestionLevel.includes(item.value))}
                            >
                                {item.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* 버튼 */}
                <div className="flex gap-3 pt-2">
                    <button
                        onClick={() => setFilters(initialFilters)}
                        className="flex-1 py-3 rounded-2xl border border-gray-200 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors"
                    >
                        초기화
                    </button>
                    <button
                        onClick={() => {
                            onApply(filters);
                            onClose();
                        }}
                        className="flex-1 py-3 rounded-2xl bg-gray-800 text-white text-sm font-medium hover:bg-gray-900 transition-colors"
                    >
                        적용하기
                    </button>
                </div>
            </div>
        </div>
    );
}
