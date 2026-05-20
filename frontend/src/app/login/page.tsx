"use client";

export default function LoginPage() {
    const handleKakaoLogin = () => {
        // 백엔드 OAuth 엔드포인트로 이동
        window.location.href =
            `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/V1/auth/oauth/kakao`;
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <div className="text-center">
                <h1 className="text-2xl font-bold mb-10">카공데이</h1>

                <div className="space-y-4 w-72">
                    {/* 카카오 로그인 */}
                    <button
                        onClick={handleKakaoLogin}
                        className="w-full py-3 rounded-lg font-semibold bg-yellow-400 text-black hover:bg-yellow-500"
                    >
                        <div className="flex items-center justify-center gap-2">
                            <span>💬</span>
                            카카오톡으로 계속하기
                        </div>
                    </button>
                </div>
            </div>
        </div>
    );
}