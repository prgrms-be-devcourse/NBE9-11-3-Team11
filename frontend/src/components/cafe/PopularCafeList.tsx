"use client";

import { useState, useEffect } from "react";
import { Heart } from "lucide-react";
import { CafeListResponse } from "@/types/cafe";
import { fetchCafeList } from "@/lib/api/cafe";

interface PopularCafeListProps {
    onCafeSelect: (cafeId: number) => void;
    bounds: {
        swLat: number;
        swLng: number;
        neLat: number;
        neLng: number;
    } | null;
}

export default function PopularCafeList({ onCafeSelect, bounds }: PopularCafeListProps) {
    const [cafes, setCafes] = useState<CafeListResponse[]>([]);

    useEffect(() => {
        if (!bounds) return;

        const loadPopularCafes = async () => {
            try {
                const data = await fetchCafeList({
                    swLat: bounds.swLat,
                    swLng: bounds.swLng,
                    neLat: bounds.neLat,
                    neLng: bounds.neLng,
                });
                const sorted = [...data]
                    .sort((a, b) => {
                        if (b.wishlistCount !== a.wishlistCount) {
                            return b.wishlistCount - a.wishlistCount; // 찜수 내림차순
                        }
                        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(); // 찜수 같으면 최신순
                    })
                    .slice(0, 3);
                setCafes(sorted);
            } catch (e) {
                console.error(e);
            }
        };
        loadPopularCafes();
    }, [bounds]);

    if (cafes.length === 0) return null;

    return (
        <div className="fixed top-30 left-4 z-40 w-64 bg-white/50 backdrop-blur-sm rounded-3xl shadow-xl overflow-hidden border border-gray-100">

            {/* 헤더 */}
            <div className="px-5 py-3 border-b">
                <h2 className="font-semibold text-gray-900 text-sm">인기 카페</h2>
                <p className="text-xs text-gray-400 mt-0.5">현재 지도 기준 찜 많은 순</p>
            </div>

            {/* 목록 */}
            <div className="p-3 space-y-2">
                {cafes.map((item, index) => {
                    const { cafe, wishlistCount } = item;
                    return (
                        <button
                            key={cafe.cafeId}
                            onClick={() => onCafeSelect(cafe.cafeId)}
                            className="w-full flex items-center gap-3 bg-white rounded-2xl p-3 shadow-sm hover:shadow-md transition-shadow text-left"
                        >
                            {/* 순위 */}
                            <div className={`w-6 h-6 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0
                                ${index === 0 ? "bg-amber-100 text-amber-600" :
                                    index === 1 ? "bg-gray-100 text-gray-500" :
                                        "bg-orange-100 text-orange-500"}`}
                            >
                                {index + 1}
                            </div>

                            {/* 정보 */}
                            <div className="flex-1 min-w-0">
                                <p className="font-semibold text-gray-900 text-xs truncate">{cafe.name}</p>
                                <p className="text-xs text-gray-400 truncate mt-0.5">{cafe.address.split(" ").slice(0, 3).join(" ")}</p>
                            </div>

                            {/* 찜 수 */}
                            <div className="flex items-center gap-1 flex-shrink-0">
                                <Heart size={10} className="text-red-400" fill="#f87171" />
                                <span className="text-xs text-gray-400">{wishlistCount.toLocaleString()}</span>
                            </div>
                        </button>
                    );
                })}
            </div>
        </div>
    );
}