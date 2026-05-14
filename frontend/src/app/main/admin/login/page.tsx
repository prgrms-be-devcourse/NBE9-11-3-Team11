"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { loginAdmin } from "@/app/api/auth";

export default function AdminLoginPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    try {
      await loginAdmin(email, password);

      // 로그인 성공 시 이동
      router.replace("/main/admin/cafe");
    } catch (error) {
      console.error(error);
      alert("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="bg-white p-10 rounded-lg shadow-md w-[400px]">
        <h2 className="text-center text-lg font-bold mb-8">
          관리자 로그인
        </h2>

        <div className="space-y-6">
          {/* 이메일 */}
          <div>
            <label className="text-sm text-gray-600">
              관리자 이메일
            </label>
            <input
              type="email"
              placeholder="admin@ideal.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full mt-2 border px-3 py-2 rounded-md focus:outline-none focus:ring-2 focus:ring-black"
            />
          </div>

          {/* 비밀번호 */}
          <div>
            <label className="text-sm text-gray-600">
              비밀번호
            </label>
            <input
              type="password"
              placeholder="********"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full mt-2 border px-3 py-2 rounded-md focus:outline-none focus:ring-2 focus:ring-black"
            />
          </div>

          {/* 로그인 버튼 */}
          <button
            onClick={handleLogin}
            className="w-full py-3 rounded-lg font-semibold bg-black text-white hover:bg-gray-800"
          >
            로그인
          </button>
        </div>
      </div>
    </div>
  );
}