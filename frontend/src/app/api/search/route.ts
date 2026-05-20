import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
    const { searchParams } = new URL(req.url);
    const query = searchParams.get("query");

    if (!query) return NextResponse.json({ documents: [] });

    const headers = {
        Authorization: `KakaoAK ${process.env.KAKAO_REST_API_KEY}`,
    };

    // 주소 검색 + 지하철역 키워드 검색 동시 호출
    const [addressRes, keywordRes] = await Promise.all([
        fetch(
            `https://dapi.kakao.com/v2/local/search/address.json?query=${encodeURIComponent(query)}&size=3`,
            { headers }
        ),
        fetch(
            `https://dapi.kakao.com/v2/local/search/keyword.json?query=${encodeURIComponent(query)}&category_group_code=SW8&size=3`,
            { headers }
        ),
    ]);

    const [addressData, keywordData] = await Promise.all([
        addressRes.json(),
        keywordRes.json(),
    ]);

    // 주소 결과 가공
    const addressResults = (addressData.documents || []).map((doc: any) => ({
        place_name: doc.address_name,
        address_name: doc.address_name,
        x: doc.x,
        y: doc.y,
        type: "address",
    }));

    // 지하철역 결과 가공
    const subwayResults = (keywordData.documents || []).map((doc: any) => ({
        place_name: doc.place_name,
        address_name: doc.address_name,
        x: doc.x,
        y: doc.y,
        type: "subway",
    }));

    // 합쳐서 반환
    const combined = [...addressResults, ...subwayResults];
    return NextResponse.json({ documents: combined });
}