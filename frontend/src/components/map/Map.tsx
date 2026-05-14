"use client";

import { useEffect, useRef, useCallback } from "react";
import { CafeListResponse } from "@/types/cafe";
import { fetchCafeList } from "@/lib/api/cafe";

declare global {
    interface Window {
        kakao: any;
    }
}

interface KakaoMapProps {
    onCafeSelect: (cafeId: number) => void;
    center: { lat: number; lng: number } | null;
    filters: {
        franchise: string[];
        hasWifi: boolean | null;
        hasOutlet: boolean | null;
        hasToilet: boolean | null;
        hasSeparateSpace: boolean | null;
        floorCount: string[];
        congestionLevel: string[];
    };
    onBoundsChange: (bounds: { swLat: number; swLng: number; neLat: number; neLng: number }) => void;
}

export default function KakaoMap({ onCafeSelect, center, filters, onBoundsChange }: KakaoMapProps) {

    const mapRef = useRef<HTMLDivElement>(null);
    const mapInstance = useRef<any>(null);
    const clustererInstance = useRef<any>(null);
    const fetchAndUpdateMarkersRef = useRef<() => void>(() => { });

    const getOffsetByZoom = (level: number) => {
        if (level === 1) return 0.0022;
        if (level === 2) return 0.0045;
        if (level === 3) return 0.009;
        if (level === 4) return 0.018;
        if (level === 5) return 0.027;
        if (level === 6) return 0.036;
        if (level === 7) return 0.054;
        if (level === 8) return 0.09;
        if (level === 9) return 0.18;
        if (level === 10) return 0.27;
        if (level === 11) return 0.45;
        if (level === 12) return 0.9;
        if (level === 13) return 1.8;
        return 3.6;
    };

    const fetchAndUpdateMarkers = useCallback(async () => {
        if (!mapInstance.current) return;

        const bounds = mapInstance.current.getBounds();
        const sw = bounds.getSouthWest();
        const ne = bounds.getNorthEast();

        const centerLat = (sw.getLat() + ne.getLat()) / 2;
        const centerLng = (sw.getLng() + ne.getLng()) / 2;

        const level = mapInstance.current.getLevel();
        const offset = getOffsetByZoom(level);

        onBoundsChange({
            swLat: centerLat - offset,
            swLng: centerLng - offset,
            neLat: centerLat + offset,
            neLng: centerLng + offset,
        });

        try {
            const cafes = await fetchCafeList({
                swLat: centerLat - offset,
                swLng: centerLng - offset,
                neLat: centerLat + offset,
                neLng: centerLng + offset,
                hasWifi: filters.hasWifi ?? undefined,
                hasOutlet: filters.hasOutlet ?? undefined,
                hasToilet: filters.hasToilet ?? undefined,
                hasSeparateSpace: filters.hasSeparateSpace ?? undefined,
                floorCounts: filters.floorCount.length > 0 ? filters.floorCount : undefined,
                congestionLevels: filters.congestionLevel.length > 0 ? filters.congestionLevel : undefined,
                franchises: filters.franchise.length > 0 ? filters.franchise : undefined,
            });

            if (!cafes || !Array.isArray(cafes)) return;
            updateMarkers(cafes);
        } catch (e) {
            console.error(e);
        }
    }, [filters, onBoundsChange]);

    // fetchAndUpdateMarkers 최신 버전을 ref에 저장
    useEffect(() => {
        fetchAndUpdateMarkersRef.current = fetchAndUpdateMarkers;
    }, [fetchAndUpdateMarkers]);

    const updateMarkers = (cafes: CafeListResponse[]) => {
        if (clustererInstance.current) {
            clustererInstance.current.clear();
        }

        const markers = cafes.map((item) => {
            const { cafe } = item;
            const position = new window.kakao.maps.LatLng(cafe.latitude, cafe.longitude);
            const marker = new window.kakao.maps.Marker({ position, title: cafe.name });

            window.kakao.maps.event.addListener(marker, "click", () => {
                onCafeSelect(cafe.cafeId);
            });

            return marker;
        });

        clustererInstance.current.addMarkers(markers);
    };

    const initMap = useCallback(() => {
        if (!mapRef.current || !window.kakao?.maps || mapInstance.current) return;

        const options = {
            center: new window.kakao.maps.LatLng(37.5665, 126.9780),
            level: 3,
        };

        mapInstance.current = new window.kakao.maps.Map(mapRef.current, options);

        clustererInstance.current = new window.kakao.maps.MarkerClusterer({
            map: mapInstance.current,
            averageCenter: true,
            minLevel: 6,
            gridSize: 60,
        });

        window.kakao.maps.event.addListener(mapInstance.current, "dragend", () => fetchAndUpdateMarkersRef.current());
        window.kakao.maps.event.addListener(mapInstance.current, "zoom_changed", () => fetchAndUpdateMarkersRef.current());

        fetchAndUpdateMarkersRef.current();
    }, []);

    useEffect(() => {
        if (!center || !mapInstance.current) return;
        const moveLatLng = new window.kakao.maps.LatLng(center.lat, center.lng);
        mapInstance.current.setCenter(moveLatLng);
        mapInstance.current.setLevel(3);
        fetchAndUpdateMarkersRef.current();
    }, [center]);

    useEffect(() => {
        if (!mapInstance.current) return;
        fetchAndUpdateMarkers();
    }, [filters, fetchAndUpdateMarkers]);

    useEffect(() => {
        if (window.kakao?.maps) {
            window.kakao.maps.load(initMap);
            return;
        }

        const script = document.createElement("script");
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY}&autoload=false&libraries=clusterer`;
        script.async = true;
        script.onload = () => {
            window.kakao.maps.load(initMap);
        };
        document.head.appendChild(script);

        return () => {
            if (script.parentNode) {
                script.parentNode.removeChild(script);
            }
        };
    }, [initMap]);

    useEffect(() => {
        const handleRelayout = () => {
            if (mapInstance.current) {
                mapInstance.current.relayout();
                setTimeout(() => mapInstance.current?.relayout(), 150);
                setTimeout(() => mapInstance.current?.relayout(), 300);
            }
        };

        const observer = new ResizeObserver(handleRelayout);
        if (mapRef.current) {
            observer.observe(mapRef.current);
        }

        const initialTimer = setTimeout(handleRelayout, 600);

        return () => {
            observer.disconnect();
            clearTimeout(initialTimer);
        };
    }, []);

    return (
        <div
            ref={mapRef}
            style={{ width: "100%", height: "100vh" }}
            className="bg-[#f2f0eb]"  // 여기만 변경
        />
    );
}