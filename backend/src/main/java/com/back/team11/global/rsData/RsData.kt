package com.back.team11.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore

data class RsData<T>(
    val msg: String,
    val resultCode: String,
    val data: T? = null, // 기본값 추가
) {

    @get:JsonIgnore
    val statusCode: Int
        get() = resultCode.substringBefore("-").toInt()
}
/* 기존 코드 - split 으로 분리 -> 배열 변환 -> 첫번 째 요소를 Int로 전환
   하이픈(-)의 앞 문자열만 추출 후 Int 전환
   의도 명확하고 간결 + 하이픈이 없을 경우 문자열 전체 반환하여 더 안전함
 */