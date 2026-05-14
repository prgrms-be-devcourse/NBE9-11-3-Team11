import { CafeListResponse, CafeDetailResponse, ReviewResponse, WishlistResponse, CafeRequest, PageResponse } from "@/types/cafe";

interface RsData<T> {
    msg: string;
    resultCode: string;
    data: T;
}

const BASE_URL = process.env.NEXT_PUBLIC_API_URL;

// 카페 목록 조회
export const fetchCafeList = async (params: {
    swLat: number;
    swLng: number;
    neLat: number;
    neLng: number;
    hasWifi?: boolean;
    hasOutlet?: boolean;
    hasToilet?: boolean;
    hasSeparateSpace?: boolean;
    floorCounts?: string[];
    congestionLevels?: string[];
    franchises?: string[];
}): Promise<CafeListResponse[]> => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value === undefined || value === null) return;
        if (Array.isArray(value)) {
            value.forEach((v) => query.append(key, v)); // ?franchises=A&franchises=B
        } else {
            query.append(key, String(value));
        }
    });

    const res = await fetch(`${BASE_URL}/api/V1/cafe?${query.toString()}`, {
        credentials: "include",
    });
    const data: RsData<CafeListResponse[]> = await res.json();
    return data.data;
};

// 카페 단건 조회
export const fetchCafeDetail = async (cafeId: number): Promise<CafeDetailResponse> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/${cafeId}`, {
        credentials: "include",
    });
    const data: RsData<CafeDetailResponse> = await res.json();
    return data.data;
};

// 리뷰 작성
export const createReview = async (cafeId: number, content: string): Promise<RsData<null>> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/${cafeId}/reviews`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ content }),
    });
    const data: RsData<null> = await res.json();
    return data;
};

// 리뷰 페이징 조회
export const fetchCafeReviewsPage = async (cafeId: number, page = 0, size = 10): Promise<PageResponse<ReviewResponse>> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/${cafeId}/reviews/page?page=${page}&size=${size}`, {
        credentials: "include",
    });
    const data: RsData<PageResponse<ReviewResponse>> = await res.json();
    return data.data;
};

export const updateReview = async (
    cafeId: number,
    reviewId: number,
    content: string
  ) => {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/V1/cafe/${cafeId}/reviews/${reviewId}`,
      {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          content,
        }),
      }
    );
  
    if (!res.ok) {
      throw new Error("리뷰 수정 실패");
    }
  
    return res.json();
  };


// 리뷰 삭제
export const deleteReview = async (cafeId: number, reviewId: number): Promise<RsData<null>> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/${cafeId}/reviews/${reviewId}`, {
        method: "DELETE",
        credentials: "include",
    });
    const data: RsData<null> = await res.json();
    return data;
};

// 찜 추가
export const addWishlist = async (cafeId: number): Promise<RsData<null>> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/${cafeId}/wishlist`, {
        method: "POST",
        credentials: "include",
    });
    const data: RsData<null> = await res.json();
    return data;
};

// 찜 취소
export const removeWishlist = async (cafeId: number): Promise<RsData<null>> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/${cafeId}/wishlist`, {
        method: "DELETE",
        credentials: "include",
    });
    const data: RsData<null> = await res.json();
    return data;
};

// 찜 목록 조회
export const fetchWishlist = async (page = 0, size = 10): Promise<PageResponse<WishlistResponse>> => {
    const res = await fetch(`${BASE_URL}/api/V1/member/me/wishlist?page=${page}&size=${size}`, {
        credentials: "include",
    });
    const data: RsData<PageResponse<WishlistResponse>> = await res.json();
    return data.data;
};

// 제보용

export const reportCafe = async (form: CafeRequest): Promise<RsData<null>> => {
    const res = await fetch(`${BASE_URL}/api/V1/cafe/report`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify(form),
    });
    const data: RsData<null> = await res.json();
    return data;
};