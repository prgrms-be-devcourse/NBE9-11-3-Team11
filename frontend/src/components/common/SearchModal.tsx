"use client";

import { useState, useEffect } from "react";
import { Search, X, MapPin } from "lucide-react";

interface SearchResult {
    place_name: string;
    address_name: string;
    x: string;
    y: string;
    type: "address" | "subway";
}

interface SearchModalProps {
    onClose: () => void;
    onSelect: (lat: number, lng: number) => void;
}

export default function SearchModal({ onClose, onSelect }: SearchModalProps) {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!query.trim()) {
            setResults([]);
            return;
        }

        const timer = setTimeout(async () => {
            setLoading(true);
            try {
                const res = await fetch(`/api/search?query=${encodeURIComponent(query)}`);
                const data = await res.json();
                setResults(data.documents || []);
            } catch (e) {
                console.error(e);
            } finally {
                setLoading(false);
            }
        }, 300);

        return () => clearTimeout(timer);
    }, [query]);

    return (
        <div
            className="fixed inset-0 z-50 flex items-start justify-center pt-32 bg-black/20 backdrop-blur-sm"
            onClick={onClose}
        >
            <div
                className="w-[480px] bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl overflow-hidden"
                onClick={(e) => e.stopPropagation()}
            >
                {/* 입력창 */}
                <div className="flex items-center gap-3 px-5 py-4">
                    <Search size={20} className="text-gray-400 flex-shrink-0" />
                    <input
                        autoFocus
                        type="text"
                        placeholder="지역 및 지하철 역을 검색하세요"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        className="flex-1 text-gray-900 placeholder-gray-400 bg-transparent outline-none text-sm"
                    />
                    {query && (
                        <button onClick={() => setQuery("")}>
                            <X size={18} className="text-gray-400 hover:text-gray-600" />
                        </button>
                    )}
                </div>

                {/* 결과 목록 */}
                {results.length > 0 && (
                    <div className="border-t border-gray-100">
                        {results.map((result, index) => (
                            <button
                                key={index}
                                onClick={() => {
                                    onSelect(parseFloat(result.y), parseFloat(result.x));
                                    onClose();
                                }}
                                className="w-full flex items-center gap-3 px-5 py-3 hover:bg-gray-50 transition-colors text-left"
                            >
                                <div className="w-8 h-8 bg-amber-100 rounded-xl flex items-center justify-center flex-shrink-0 text-sm">
                                    {result.type === "subway" ? "🚇" : <MapPin size={14} className="text-amber-600" />}
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{result.place_name}</p>
                                    {result.type === "subway" && (
                                        <p className="text-xs text-gray-500 mt-0.5">{result.address_name}</p>
                                    )}
                                </div>
                            </button>
                        ))}
                    </div>
                )}

                {/* 로딩 */}
                {loading && (
                    <div className="border-t border-gray-100 px-5 py-4 text-sm text-gray-400 text-center">
                        검색 중...
                    </div>
                )}

                {/* 결과 없음 */}
                {!loading && query && results.length === 0 && (
                    <div className="border-t border-gray-100 px-5 py-4 text-sm text-gray-400 text-center">
                        검색 결과가 없어요
                    </div>
                )}
            </div>
        </div>
    );
}