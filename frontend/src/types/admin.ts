// 관리자 타입 정의 추가

// 카페 승인 상태 (승인대기/승인완료/거절)
export type CafeStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

// 카페 종류 (프랜차이즈/개인카페)
export type CafeType = 'FRANCHISE' | 'INDIVIDUAL';

// 프랜차이즈 종류 (스타벅스/메가커피/해당없음)
// cafe.ts 에서 FranchiseType 으로 쓰고 있어서 맞춤
export type FranchiseType = 'STARBUCKS' | 'MEGA_COFFEE' | 'EDIYA' | 'COMPOSE' | 'TWOSOME' | 'PAIK_DABANG' | 'THE_VENTI' | 'NONE';

// 혼잡도 ( 여유/보통/혼잡)
export type CongestionLevel = 'LOW' | 'MEDIUM' | 'HIGH';

// 층수 (1층/2층/3층이상)
export type FloorCount = 'ONE' | 'TWO' | 'THREE_OR_MORE';

// 공통 기본 정보
export interface AdminCafeBaseInfo {
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

// 백엔드에서 프론트로 오는 데이터 형태
export interface AdminCafe {
    cafe: AdminCafeBaseInfo;      // 공통 기본 정보
    status: CafeStatus;           // 승인 상태 (관리자만 볼 수 있음)
    createdAt: string;            // 등록일
}



// 카페 수정할 때 백엔드로 보내는 데이터 형태
// 수정은 바뀐 것만 보내도 돼서 전부 ? (선택)
export interface CafeUpdateRequest {
    name?: string;
    address?: string;
    latitude?: number;
    longitude?: number;
    phone?: string;
    description?: string;
    type?: CafeType;
    franchise?: FranchiseType;
    hasToilet?: boolean;
    hasOutlet?: boolean;
    hasWifi?: boolean;
    floorCount?: FloorCount;
    hasSeparateSpace?: boolean;
    congestionLevel?: CongestionLevel;
    imageUrl?: string;
}


// 목록 조회할 때 백엔드가 페이지 형태로 데이터를 줌
export interface PageResponse<T> {
    content: T[];          // 실제 데이터 목록
    totalElements: number; // 전체 데이터 개수
    totalPages: number;    // 전체 페이지 수
    currentPage: number;   // 현재 페이지 번호
}