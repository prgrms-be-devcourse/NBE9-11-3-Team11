package com.back.team11.global.exception

import com.back.team11.global.rsData.RsData
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<RsData<Unit>> =
        ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(RsData(e.errorCode.message, e.errorCode.code))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Unit>> =
        ResponseEntity
            .badRequest()
            .body(RsData(
                e.bindingResult.fieldErrors.first().defaultMessage ?: "잘못된 입력값입니다.",
                "400-1"
            ))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<RsData<Unit>> =
        ResponseEntity
            .badRequest()
            .body(RsData("잘못된 입력값입니다.", "400-1"))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<RsData<Unit>> =
        ResponseEntity
            .internalServerError()
            .body(RsData("서버 오류가 발생했습니다.", "500"))
}