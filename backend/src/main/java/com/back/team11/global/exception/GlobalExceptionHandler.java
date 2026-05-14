package com.back.team11.global.exception;

import com.back.team11.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class) // 커스텀 오류 설정
    public ResponseEntity<RsData<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new RsData<>(errorCode.getMessage(),errorCode.getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // Valid 검증 오류에서 첫번째 오류
    public ResponseEntity<RsData<Void>> handleValidException(MethodArgumentNotValidException e) {

        String message = e.getBindingResult()  // 검증 결과 가져오기
                .getFieldErrors()              // 필드 에러 목록
                .get(0)                        // 첫 번째 에러만
                .getDefaultMessage();          // 에러 메시지 꺼내기

        return ResponseEntity
                .badRequest()
                .body(new RsData<>(message, "400-1"));
    }


    // @Valid 이전 단계인 JSON 역직렬화 실패 처리
    // ex) Enum에 없는 값, Boolean에 문자열 입력 등
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        return ResponseEntity
                .badRequest()
                .body(new RsData<>("잘못된 입력값입니다.", "400-1"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleException(Exception e) { //Custom Error, Valid 검증 오류도 아닌 예외들
        return ResponseEntity
                .internalServerError()   // 500 반환
                .body(new RsData<>("서버 오류가 발생했습니다.","500"));
    }
}
