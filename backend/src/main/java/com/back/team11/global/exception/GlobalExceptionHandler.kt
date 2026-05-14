package com.back.team11.global.exception

import com.back.team11.global.rsData.RsData
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class) // 커스텀 오류 설정
    fun handleCustomException(e: CustomException): ResponseEntity<RsData<Void?>> {
        val errorCode = e.errorCode
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body<RsData<Void?>>(RsData<Void?>(errorCode.message, errorCode.code))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class) // Valid 검증 오류에서 첫번째 오류
    fun handleValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Void?>> {
        val message = e.bindingResult // 검증 결과 가져오기
            .fieldErrors // 필드 에러 목록
            .first()// 첫 번째 에러만
            .defaultMessage // 에러 메시지 꺼내기

        return ResponseEntity
            .badRequest()
            .body<RsData<Void?>>(RsData<Void?>(message!!, "400-1"))
    }


    // @Valid 이전 단계인 JSON 역직렬화 실패 처리
    // ex) Enum에 없는 값, Boolean에 문자열 입력 등
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException?
    ): ResponseEntity<RsData<Void?>> {
        return ResponseEntity
            .badRequest()
            .body<RsData<Void?>>(RsData<Void?>("잘못된 입력값입니다.", "400-1"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception?): ResponseEntity<RsData<Void?>> { //Custom Error, Valid 검증 오류도 아닌 예외들
        return ResponseEntity
            .internalServerError() // 500 반환
            .body<RsData<Void?>>(RsData<Void?>("서버 오류가 발생했습니다.", "500"))
    }
}
