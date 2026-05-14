"use client";

import { useAuthStore } from "@/store/authStore";
import { useRouter } from "next/navigation";
import { logoutApi } from "@/lib/api/auth";

interface HeaderProps {
    onSearchClick: () => void;
    onReportClick: () => void;
}

export default function Header({ onSearchClick, onReportClick }: HeaderProps) {
    const { isLoggedIn, member, logout } = useAuthStore();
    const router = useRouter();

    const handleLogout = async () => {
        await logoutApi();
        logout();
        window.location.href = "/";
    };

    return (
        <header className="fixed top-4 left-4 right-4 z-50 bg-white/50 backdrop-blur-sm border border-gray-100 rounded-2xl">
            <div className="flex items-center justify-between px-6 py-3">

                {/* 로고 */}
                <div className="flex items-center gap-2">
                    <div>
                        <span className="font-bold text-lg text-gray-900">카공데이</span>
                        <p className="text-xs text-gray-500">카공하기 좋은 장소</p>
                    </div>
                </div>

                {/* 버튼 */}
                <div className="flex items-center gap-2">
                    {isLoggedIn ? (
                        <>
                            <span className="text-sm font-medium text-gray-700">
                                {member?.nickname}
                            </span>
                            <button
                                onClick={handleLogout}
                                className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-xl transition-colors"
                            >
                                로그아웃
                            </button>
                        </>
                    ) : (
                        <button
                            onClick={() => router.push("/login")}
                            className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-xl transition-colors"
                        >
                            로그인
                        </button>
                    )}
                    <button
                        onClick={onReportClick}
                        className="px-4 py-2 text-sm font-medium text-white bg-gray-600 hover:bg-gray-700 rounded-xl transition-colors"
                    >
                        제보하기
                    </button>
                </div>
            </div>
        </header>
    );
}