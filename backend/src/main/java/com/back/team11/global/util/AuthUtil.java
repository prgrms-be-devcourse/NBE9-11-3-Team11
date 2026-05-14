package com.back.team11.global.util;

import com.back.team11.global.exception.CustomException;
import com.back.team11.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    public Long getCurrentMemberId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return (Long) authentication.getPrincipal();
    }

    public Long getCurrentMemberIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return (Long) authentication.getPrincipal();
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
