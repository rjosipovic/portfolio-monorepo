package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.auth.JwtUserPrincipal;
import com.playground.challenge_manager.challenge.services.interfaces.UserIdentityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class JwtUserIdentityService implements UserIdentityService {

    private static final String USER_ID_CLAIM = "userId";

    @Override
    public UUID getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(JwtUserPrincipal.class::isInstance)
                .map(JwtUserPrincipal.class::cast)
                .map(principal -> UUID.fromString(principal.getClaims().get(USER_ID_CLAIM).toString()))
                .orElseThrow(() -> new IllegalStateException("Cannot determine current user. No valid authentication principal found."));
    }
}
