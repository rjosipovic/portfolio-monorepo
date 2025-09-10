package com.playground.user_manager.user.model;

import lombok.*;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    String id;
    String alias;
}
