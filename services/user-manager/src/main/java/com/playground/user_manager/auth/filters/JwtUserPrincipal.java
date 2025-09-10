package com.playground.user_manager.auth.filters;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUserPrincipal implements Serializable {

    String email;
    @Singular
    Map<String, Object> claims;
}

