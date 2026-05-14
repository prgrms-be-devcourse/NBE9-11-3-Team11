"use client";

import dynamic from "next/dynamic";
import { useState, useCallback, useEffect, useRef } from "react";
import { CafeDetailResponse } from "@/types/cafe";
import CafeDetail from "@/components/cafe/CafeDetail";
import Header from "@/components/common/Header";
import SearchModal from "@/components/common/SearchModal";
import WishlistPanel from "@/components/cafe/WishlistPanel";
import FilterModal, { FilterState } from "@/components/common/FilterModal";
import ReportModal from "@/components/cafe/ReportModal";
import PopularCafeList from "@/components/cafe/PopularCafeList";
import { Search, Heart, SlidersHorizontal, X } from "lucide-react";
import { fetchCafeDetail } from "@/lib/api/cafe";
import { useAuthStore } from "@/store/authStore";
import { fetchMe } from "@/lib/api/auth";
import { useRouter } from "next/navigation";

const KakaoMap = dynamic(() => import("@/components/map/Map"), { ssr: false });

const initialFilters: FilterState = {
  type: [],
  franchise: [],
  hasWifi: null,
  hasOutlet: null,
  hasToilet: null,
  hasSeparateSpace: null,
  floorCount: [],
  congestionLevel: [],
};

export default function Home() {
  const [selectedCafe, setSelectedCafe] = useState<CafeDetailResponse | null>(null);
  const [showSearch, setShowSearch] = useState(false);
  const [showWishlist, setShowWishlist] = useState(false);
  const [showFilter, setShowFilter] = useState(false);
  const [filters, setFilters] = useState<FilterState>(initialFilters);
  const [mapCenter, setMapCenter] = useState<{ lat: number; lng: number } | null>(null);
  const [showReport, setShowReport] = useState(false);
  const prevIsLoggedIn = useRef<boolean | null>(null);
  const [mapBounds, setMapBounds] = useState<{
    swLat: number;
    swLng: number;
    neLat: number;
    neLng: number;
  } | null>(null);

  const { isLoggedIn, setMember } = useAuthStore();

  const router = useRouter();

  useEffect(() => {
    const checkAuth = async () => {
      const member = await fetchMe();
      setMember(member);
    };
    checkAuth();
  }, []);

  useEffect(() => {
    if (prevIsLoggedIn.current === true && !isLoggedIn) {
      setSelectedCafe(null);
      setShowWishlist(false);
    }
    prevIsLoggedIn.current = isLoggedIn;
  }, [isLoggedIn]);

  const handleSearchSelect = (lat: number, lng: number) => {
    setMapCenter({ lat, lng });
  };

  const handleFilterApply = (newFilters: FilterState) => {
    setFilters(newFilters);
  };

  const handleBoundsChange = useCallback((bounds: {
    swLat: number;
    swLng: number;
    neLat: number;
    neLng: number;
  }) => {
    setMapBounds(bounds);
  }, []);

  // 마커 클릭용 (지도 이동 없음)
  const handleMarkerSelect = async (cafeId: number) => {
    try {
      const cafe = await fetchCafeDetail(cafeId);
      setSelectedCafe(cafe);
    } catch (e) {
      console.error(e);
    }
  };

  // 찜 목록, 인기 카페 클릭용 (지도 이동 있음)
  const handleCafeSelect = async (cafeId: number) => {
    try {
      const cafeData = await fetchCafeDetail(cafeId);
      const { cafe } = cafeData;
      setSelectedCafe(cafeData);
      setShowWishlist(false);
      setMapCenter({ lat: cafe.latitude, lng: cafe.longitude });
    } catch (e) {
      console.error(e);
    }
  };

  const activeFilterTags: { label: string; onRemove: () => void }[] = [
    ...(filters.type.includes("INDIVIDUAL") ? [{
      label: "개인카페",
      onRemove: () => setFilters((prev) => ({ ...prev, type: prev.type.filter((t) => t !== "INDIVIDUAL") })),
    }] : []),
    ...filters.franchise.map((v) => ({
      label: v === "STARBUCKS" ? "스타벅스" :
        v === "MEGA_COFFEE" ? "메가커피" :
          v === "EDIYA" ? "이디야" :
            v === "COMPOSE" ? "컴포즈" :
              v === "TWOSOME" ? "투썸" :
                v === "PAIK_DABANG" ? "빽다방" :
                  v === "THE_VENTI" ? "더벤티" : v,
      onRemove: () => setFilters((prev) => {
        const newFranchise = prev.franchise.filter((f) => f !== v);
        return {
          ...prev,
          franchise: newFranchise,
          type: newFranchise.length === 0 ? prev.type.filter((t) => t !== "FRANCHISE") : prev.type,
        };
      }),
    })),
    ...(filters.type.includes("FRANCHISE") && filters.franchise.length === 0 ? [{
      label: "프랜차이즈",
      onRemove: () => setFilters((prev) => ({ ...prev, type: prev.type.filter((t) => t !== "FRANCHISE") })),
    }] : []),
    ...(filters.hasWifi ? [{ label: "와이파이", onRemove: () => setFilters((prev) => ({ ...prev, hasWifi: null })) }] : []),
    ...(filters.hasOutlet ? [{ label: "콘센트", onRemove: () => setFilters((prev) => ({ ...prev, hasOutlet: null })) }] : []),
    ...(filters.hasToilet ? [{ label: "화장실", onRemove: () => setFilters((prev) => ({ ...prev, hasToilet: null })) }] : []),
    ...(filters.hasSeparateSpace ? [{ label: "분리된 공간", onRemove: () => setFilters((prev) => ({ ...prev, hasSeparateSpace: null })) }] : []),
    ...filters.floorCount.map((v) => ({
      label: v === "ONE" ? "1층" : v === "TWO" ? "2층" : "3층 이상",
      onRemove: () => setFilters((prev) => ({ ...prev, floorCount: prev.floorCount.filter((f) => f !== v) })),
    })),
    ...filters.congestionLevel.map((v) => ({
      label: v === "LOW" ? "여유" : v === "MEDIUM" ? "보통" : "혼잡",
      onRemove: () => setFilters((prev) => ({ ...prev, congestionLevel: prev.congestionLevel.filter((f) => f !== v) })),
    })),
  ];

  return (
    <main className="relative flex-1">
      <Header
        onSearchClick={() => setShowSearch(true)}
        onReportClick={() => {
          if (!isLoggedIn) {
            router.push("/login");
            return;
          }
          setShowReport(true);
        }}
      />
      <KakaoMap
        onCafeSelect={handleMarkerSelect}
        center={mapCenter}
        filters={filters}
        onBoundsChange={handleBoundsChange}
      />

      {/* 우측 아이콘 버튼 그룹 */}
      <div className="fixed top-24 right-6 z-40 flex flex-col gap-2 items-end">
        <button
          onClick={() => setShowSearch(true)}
          className="w-10 h-10 bg-white/80 backdrop-blur-sm border border-gray-100 rounded-2xl shadow-md flex items-center justify-center text-gray-600 hover:text-gray-900 hover:bg-white transition-colors"
        >
          <Search size={18} />
        </button>
        <button
          onClick={() => {
            setShowWishlist(!showWishlist);
            setSelectedCafe(null);
          }}
          className={`w-10 h-10 bg-white/80 backdrop-blur-sm border border-gray-100 rounded-2xl shadow-md flex items-center justify-center transition-colors
            ${showWishlist ? "text-red-500" : "text-gray-600 hover:text-gray-900 hover:bg-white"}`}
        >
          <Heart size={18} fill={showWishlist ? "#ef4444" : "none"} />
        </button>
        <button
          onClick={() => setShowFilter(true)}
          className={`w-10 h-10 bg-white/80 backdrop-blur-sm border border-gray-100 rounded-2xl shadow-md flex items-center justify-center transition-colors
            ${activeFilterTags.length > 0 ? "text-gray-800" : "text-gray-600 hover:text-gray-900 hover:bg-white"}`}
        >
          <SlidersHorizontal size={18} />
        </button>

        {activeFilterTags.map((tag, index) => (
          <div
            key={index}
            className="flex items-center gap-1.5 bg-gray-800 text-white text-xs font-medium px-3 py-1.5 rounded-full shadow-md"
          >
            <span>{tag.label}</span>
            <button
              onClick={tag.onRemove}
              className="text-gray-300 hover:text-white transition-colors"
            >
              <X size={12} />
            </button>
          </div>
        ))}
      </div>

      {showWishlist && (
        <WishlistPanel
          onClose={() => setShowWishlist(false)}
          onCafeSelect={handleCafeSelect}
        />
      )}

      {!selectedCafe && !showWishlist && (
        <PopularCafeList
          onCafeSelect={handleCafeSelect}
          bounds={mapBounds}
        />
      )}

      {selectedCafe && (
        <CafeDetail
          cafeData={selectedCafe}
          onClose={() => setSelectedCafe(null)}
        />
      )}

      {showSearch && (
        <SearchModal
          onClose={() => setShowSearch(false)}
          onSelect={handleSearchSelect}
        />
      )}

      {showFilter && (
        <FilterModal
          onClose={() => setShowFilter(false)}
          onApply={handleFilterApply}
          currentFilters={filters}
        />
      )}

      {showReport && (
        <ReportModal onClose={() => setShowReport(false)} />
      )}
    </main>
  );
}