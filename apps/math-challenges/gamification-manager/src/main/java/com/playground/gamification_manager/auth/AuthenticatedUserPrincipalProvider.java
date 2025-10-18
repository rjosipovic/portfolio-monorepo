package com.playground.gamification_manager.auth;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticatedUserPrincipalProvider {

    public static UsernamePasswordAuthenticationToken get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return (UsernamePasswordAuthenticationToken) authentication;
        } else {
            return null;
        }
    }
}
