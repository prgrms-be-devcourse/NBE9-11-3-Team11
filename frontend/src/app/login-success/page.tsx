"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LoginSuccessPage() {
  const router = useRouter();

  useEffect(() => {
    // 원하는 페이지로 변경 가능
    router.replace("/");
  }, [router]);

  return (
    <div className="flex items-center justify-center h-screen">
      <p>로그인 완료! 이동 중...</p>
    </div>
  );
}