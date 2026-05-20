import { create } from "zustand";

interface Member {
    memberId: number;
    nickname: string;
    email: string;
    role: string;
}

interface AuthStore {
    member: Member | null;
    isLoggedIn: boolean;
    setMember: (member: Member | null) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
    member: null,
    isLoggedIn: false,
    setMember: (member) => set({ member, isLoggedIn: !!member }),
    logout: () => set({ member: null, isLoggedIn: false }),
}));