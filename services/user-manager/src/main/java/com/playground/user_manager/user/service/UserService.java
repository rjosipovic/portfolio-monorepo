package com.playground.user_manager.user.service;

import com.playground.user_manager.user.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    List<User> getUsersByIds(List<String> ids);
}
