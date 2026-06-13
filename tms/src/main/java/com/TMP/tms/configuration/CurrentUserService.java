package com.TMP.tms.configuration;

import com.TMP.tms.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserService {

    /**
     * Retrieves the UUID of the currently authenticated user from the request thread.
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException("No authenticated user found in the current context");
        }

        // We set the principal as a String (the UUID) in our JwtAuthenticationFilter
        return UUID.fromString(authentication.getPrincipal().toString());
    }
}