package com.back.team11.global.exception


import org.springframework.http.HttpStatus


enum class ErrorCode (
    val httpStatus: HttpStatus,
    val code: String,
    val message: String,
){
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "400-1", "잘못된 입력값입니다."),
    INVALID_COORDINATE(HttpStatus.BAD_REQUEST, "400-2", "잘못된 좌표값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401-1", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "401-2", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "401-3", "만료된 토큰입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "401-4", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "401-5", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "401-6", "만료된 리프레시 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "403-1", "접근 권한이 없습니다."),
    FORBIDDEN_REVIEW(HttpStatus.FORBIDDEN, "403-2", "리뷰 작성자만 수정/삭제할 수 있습니다."),
    FORBIDDEN_ADMIN(HttpStatus.FORBIDDEN, "403-3", "관리자만 접근할 수 있습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "404-2", "존재하지 않는 회원입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "409-1", "이미 사용중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "409-2", "이미 사용중인 닉네임입니다."),

    // Cafe
    CAFE_NOT_FOUND(HttpStatus.NOT_FOUND, "404-3", "존재하지 않는 카페입니다."),
    CAFE_ALREADY_EXISTS(HttpStatus.CONFLICT, "409-5", "이미 등록된 카페입니다."),
    CAFE_NOT_APPROVED(HttpStatus.BAD_REQUEST, "400-3", "승인되지 않은 카페입니다."),
    CAFE_ALREADY_APPROVED(HttpStatus.CONFLICT, "409-6", "이미 승인된 카페입니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "404-4", "존재하지 않는 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "409-3", "이미 리뷰를 작성한 카페입니다."),
    CAFE_ALREADY_REJECTED(HttpStatus.CONFLICT, "409-7", "이미 거절된 카페입니다."),

    // Wishlist
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "404-5", "존재하지 않는 찜 내역입니다."),
    WISHLIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "409-4", "이미 찜한 카페입니다.");

}
