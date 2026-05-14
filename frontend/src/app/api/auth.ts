import { apiFetch, API_BASE_URL } from "./client";

//관리자 로그인
export const loginAdmin = (email: string, password: string) => {
  return apiFetch("/api/V1/admin/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
};

//카카오 로그인 (리다이렉트)
export const kakaoLogin = () => {
  window.location.href = `${API_BASE_URL}/api/V1/auth/oauth/kakao`;
};