package com.back.team11.global.extension

import org.springframework.data.repository.CrudRepository
import java.util.Optional

// CrudRepository 확장함수 - Optional.orElseThrow 대신 Kotlin 스타일로 조회
// T : Any - null 불가 타입 제한
// ID : Any - null 불가 ID 타입 제한
fun <T : Any, ID : Any> CrudRepository<T, ID>.findByIdOrThrow(id: ID, exception: () -> Exception): T =
    findById(id).orElseThrow(exception)

// Optional 확장함수 - orElseThrow 대신 Kotlin 스타일로 조회
fun <T : Any> Optional<T>.getOrThrow(exception: () -> Exception): T =
    orElseThrow(exception)

// Optional 확장함수 - orElse(null) 대신 Kotlin 스타일로 null 반환
fun <T> Optional<T>.getOrNull(): T? = orElse(null)