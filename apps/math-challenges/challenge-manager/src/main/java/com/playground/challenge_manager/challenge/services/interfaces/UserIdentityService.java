package com.playground.challenge_manager.challenge.services.interfaces;

import java.util.UUID;

/**
 * A service to abstract away the details of retrieving the current user's identity.
 */
public interface UserIdentityService {
    /**
     * Retrieves the UUID of the currently authenticated user.
     *
     * @return The user's UUID.
     * @throws org.springframework.security.core.AuthenticationException if the user is not authenticated
     *         or the identity cannot be resolved.
     */
    UUID getCurrentUserId();
}