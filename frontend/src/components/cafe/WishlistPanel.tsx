"use client";

import { useState, useEffect } from "react";
import { Heart, X, ChevronLeft, ChevronRight } from "lucide-react";
import { WishlistResponse, PageResponse } from "@/types/cafe";
import { fetchWishlist } from "@/lib/api/cafe";
import { useAuthStore } from "@/store/authStore";
import { useRouter } from "next/navigation";

interface WishlistPanelProps {
    onClose: () => void;
    onCafeSelect: (cafeId: number) => void;
}

export default function WishlistPanel({ onClose, onCafeSelect }: WishlistPanelProps) {
    const [wishlistPage, setWishlistPage] = useState<PageResponse<WishlistResponse> | null>(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const { isLoggedIn } = useAuthStore();
    const router = useRouter();

    useEffect(() => {
        if (!isLoggedIn) {
            setLoading(false);
            return;
        }

        const loadWishlist = async () => {
            setLoading(true);
            try {
                const data = await fetchWishlist(currentPage, 10);
                setWishlistPage(data ?? null);
            } catch (e) {
                console.error(e);
                setWishlistPage(null);
            } finally {
                setLoading(false);
            }
        };
        loadWishlist();
    }, [isLoggedIn, currentPage]);

    const wishlists = wishlistPage?.content ?? [];
    const totalPages = wishlistPage?.totalPages ?? 0;
    const totalElements = wishlistPage?.totalElements ?? 0;

    return (
        <div className="fixed top-[20%] left-4 bottom-4 z-50 w-[25%] bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl overflow-hidden flex flex-col border border-gray-100">

            {/* 헤더 */}
            <div className="px-5 py-4 border-b flex items-center justify-between bg-white/80 flex-shrink-0">
                <div className="flex items-center gap-2">
                    <Heart className="text-red-500" size={18} fill="#ef4444" />
                    <h2 className="font-semibold text-gray-900">찜 목록</h2>
                    {isLoggedIn && (
                        <span className="text-sm text-gray-400">({totalElements})</span>
                    )}
                </div>
                <button
                    onClick={onClose}
                    className="w-8 h-8 flex items-center justify-center text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
                >
                    <X size={18} />
                </button>
            </div>

            {/* 목록 */}
            <div className="flex-1 overflow-y-auto scrollbar-hide p-4 space-y-3">
                {!isLoggedIn ? (
                    <div className="flex flex-col items-center justify-center h-full gap-3 text-center">
                        <Heart size={40} className="text-gray-200" />
                        <p className="text-sm text-gray-500">로그인 후 찜 목록을 확인할 수 있어요</p>
                        <button
                            onClick={() => router.push("/login")}
                            className="px-4 py-2 rounded-xl bg-gray-800 text-white text-sm font-medium hover:bg-gray-900 transition-colors"
                        >
                            로그인하기
                        </button>
                    </div>
                ) : loading ? (
                    <div className="flex items-center justify-center h-full">
                        <p className="text-sm text-gray-400">로딩 중...</p>
                    </div>
                ) : wishlists.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-full gap-3 text-center">
                        <Heart size={40} className="text-gray-200" />
                        <p className="text-sm text-gray-400">찜한 카페가 없습니다</p>
                    </div>
                ) : (
                    wishlists.map((wishlist) => (
                        <button
                            key={wishlist.wishlistId}
                            onClick={() => {
                                onCafeSelect(wishlist.cafeId);
                                onClose();
                            }}
                            className="w-full flex items-center gap-3 bg-white rounded-2xl p-3 shadow-sm hover:shadow-md transition-shadow text-left"
                        >
                            <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-amber-50 to-orange-50 flex items-center justify-center flex-shrink-0">
                                <span className="text-2xl">☕</span>
                            </div>
                            <div className="flex-1 min-w-0">
                                <p className="font-semibold text-gray-900 text-sm truncate">{wishlist.cafeName}</p>
                                <p className="text-xs text-gray-500 truncate mt-0.5">
                                    {new Date(wishlist.createAt).toLocaleDateString()}
                                </p>
                            </div>
                        </button>
                    ))
                )}
            </div>

            {/* 페이지 버튼 */}
            {isLoggedIn && totalPages > 1 && (
                <div className="flex-shrink-0 border-t border-gray-100">
                    <div className="flex items-center justify-center gap-1 py-2">
                        <button
                            onClick={() => setCurrentPage(prev => prev - 1)}
                            disabled={wishlistPage?.page === 1}
                            className="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                        >
                            <ChevronLeft size={14} />
                        </button>

                        {Array.from({ length: totalPages }, (_, i) => (
                            <button
                                key={i}
                                onClick={() => setCurrentPage(i)}
                                className={`w-7 h-7 flex items-center justify-center rounded-lg text-xs font-medium transition-colors
                                    ${wishlistPage?.page === i + 1
                                        ? "bg-gray-800 text-white"
                                        : "text-gray-500 hover:text-gray-700 hover:bg-gray-100"}`}
                            >
                                {i + 1}
                            </button>
                        ))}

                        <button
                            onClick={() => setCurrentPage(prev => prev + 1)}
                            disabled={!wishlistPage?.hasNext}
                            className="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                        >
                            <ChevronRight size={14} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}