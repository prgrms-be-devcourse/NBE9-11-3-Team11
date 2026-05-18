package com.back.team11.global.util

import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthUtil {

    private val authentication: Authentication?
        get() = SecurityContextHolder.getContext().authentication

    val currentMemberId: Long
        get() = authentication
            ?.takeIf { it.isAuthenticated && it.principal != "anonymousUser" }
            ?.principal as? Long
            ?: throw CustomException(ErrorCode.UNAUTHORIZED)

    val currentMemberIdOrNull: Long?
        get() = authentication
            ?.takeIf { it.isAuthenticated && it.principal != "anonymousUser" }
            ?.principal as? Long

    val isAdmin: Boolean
        get() = authentication
            ?.authorities
            ?.any { it.authority == "ROLE_ADMIN" }
            ?: false
}
