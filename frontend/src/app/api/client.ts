export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export async function apiFetch(
  url: string,
  options?: RequestInit
) {
  const res = await fetch(`${API_BASE_URL}${url}`, {
    credentials: "include", // 쿠키 포함
    headers: {
      "Content-Type": "application/json",
    },
    ...options,
  });

  if (!res.ok) {
    // 서버 메시지 있으면 같이 출력
    const text = await res.text();
    throw new Error(text || `API Error: ${res.status}`);
  }

  return res.json();
}