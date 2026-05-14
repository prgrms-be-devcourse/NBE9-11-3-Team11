package com.back.team11.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore

data class RsData<T>(
    val msg: String,
    val resultCode: String,
    val data: T?
) {
    constructor(msg: String, resultCode: String) : this(msg, resultCode, null)

    @get:JsonIgnore
    val statusCode: Int
        get() = resultCode.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
}
