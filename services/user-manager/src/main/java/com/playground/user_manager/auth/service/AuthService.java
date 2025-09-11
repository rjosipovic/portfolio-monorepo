package com.playground.user_manager.auth.service;

public interface AuthService {

    void generateAuthCode(String email);
    boolean verifyCode(String email, String code);
}
