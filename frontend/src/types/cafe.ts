// types/cafe.ts
export type CafeType = "FRANCHISE" | "INDIVIDUAL";
export type FranchiseType = "STARBUCKS" | "MEGA_COFFEE" | "EDIYA" | "COMPOSE" | "TWOSOME" | "PAIK_DABANG" | "THE_VENTI" | "NONE";
export type FloorCount = "ONE" | "TWO" | "THREE_OR_MORE";
export type CongestionLevel = "LOW" | "MEDIUM" | "HIGH";

// 공통 기본 정보 (CafeBaseInfo)
export interface CafeBaseInfo {
    cafeId: number;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    phone: string | null;
    description: string | null;
    type: CafeType;
    franchise: FranchiseType;
    hasToilet: boolean;
    hasOutlet: boolean;
    hasWifi: boolean;
    floorCount: FloorCount;
    hasSeparateSpace: boolean;
    congestionLevel: CongestionLevel;
    imageUrl: string | null;
}

export interface CafeResponse {
    cafe: CafeBaseInfo;
    wishlistCount: number; // 찜 개수
    reviewCount: number;   // 리뷰 개수
}

// 목록/마커용
export interface CafeListResponse {
    cafe: CafeBaseInfo;
    wishlistCount: number; // 찜 개수
    createdAt: string;
}

// 상세 조회용
export interface CafeDetailResponse {
    cafe: CafeBaseInfo;
    wishlistCount: number; // 찜 개수
    isWishlisted: boolean;
    createdAt: string;
}

// 리뷰용
export interface ReviewResponse {
    id: number;
    cafeId: number;
    memberId: number;
    nickname: string;
    content: string;
    createdAt: string;
}

//찜 목록
export interface WishlistResponse {
    wishlistId: number;
    cafeId: number;
    cafeName: string;
    createAt: string;
}

//제보/등록 공통 요청 타입
export interface CafeRequest {
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    phone?: string;
    description?: string;
    type: string;
    franchise: string;
    hasToilet: boolean;
    hasOutlet: boolean;
    hasWifi: boolean;
    floorCount: string;
    hasSeparateSpace: boolean;
    congestionLevel: string;
    imageUrl?: string;
}


export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
}
