package com.playground.user_manager.auth.service;

import com.playground.user_manager.auth.api.dto.RegisterUserRequest;
import com.playground.user_manager.auth.api.dto.RegisteredUser;

import java.util.Optional;

public interface RegistrationService {

    void register(RegisterUserRequest registerUserRequest);
    boolean isRegistered(String email);
    Optional<RegisteredUser> getRegisteredUser(String email);
}
