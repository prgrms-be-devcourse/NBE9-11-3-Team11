const BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export interface Member {
    memberId: number;
    nickname: string;
    email: string;
    role: string;
}

export const fetchMe = async (): Promise<Member | null> => {
    const res = await fetch(`${BASE_URL}/api/V1/auth/me`, {
        credentials: "include",
    });
    if (!res.ok) return null;
    const data = await res.json();
    return data.data;
};


export const logoutApi = async () => {
    const res = await fetch(`${BASE_URL}/api/V1/auth/logout`, {
        method: "POST",
        credentials: "include",
    });
    return res.ok;
};

export const logoutAdmin = async () => {
    await logoutApi();
    window.location.href = `/main/admin/login`;
};