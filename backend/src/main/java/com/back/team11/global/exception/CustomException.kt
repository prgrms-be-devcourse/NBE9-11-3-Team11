package com.back.team11.global.exception


class CustomException(val errorCode: ErrorCode) : RuntimeException(
    errorCode.message
)
