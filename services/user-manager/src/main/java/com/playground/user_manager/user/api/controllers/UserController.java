package com.playground.user_manager.user.api.controllers;

import com.playground.user_manager.user.api.validation.ValidUuidList;
import com.playground.user_manager.user.model.User;
import com.playground.user_manager.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false, name = "ids" ) @Valid @ValidUuidList List<String> ids) {
        var users = Optional.ofNullable(ids)
                .map(userService::getUsersByIds)
                .orElseGet(userService::getAllUsers);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/alias/{alias}")
    public ResponseEntity<User> getUserByAlias(@PathVariable String alias) {
        var user = userService.getUserByAlias(alias);
        return ResponseEntity.ok(user);
    }
}
