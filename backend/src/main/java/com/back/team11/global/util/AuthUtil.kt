package com.back.team11.global.util

import com.back.team11.global.exception.CustomException
import com.back.team11.global.exception.ErrorCode
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthUtil {
    val currentMemberId: Long?
        get() {
            val authentication =
                SecurityContextHolder.getContext().getAuthentication()

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymousUser") {
                throw CustomException(ErrorCode.UNAUTHORIZED)
            }

            return authentication.getPrincipal() as Long?
        }

    val currentMemberIdOrNull: Long?
        get() {
            val authentication =
                SecurityContextHolder.getContext().getAuthentication()

            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymousUser") {
                return null
            }

            return authentication.getPrincipal() as Long?
        }

    val isAdmin: Boolean
        get() {
            val authentication =
                SecurityContextHolder.getContext().getAuthentication()
            return authentication!!.getAuthorities()
                .stream()
                .anyMatch { authority: GrantedAuthority? -> authority!!.getAuthority() == "ROLE_ADMIN" }
        }
}
