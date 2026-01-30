package com.playground.challenge_manager.challenge.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiPaths {

    public static final String CHALLENGES = "/challenges";
    public static final String CHALLENGES_WITH_ID = CHALLENGES + "/{id}";
    public static final String CHALLENGE_STATUS = CHALLENGES_WITH_ID + "/status";
    public static final String CHALLENGE_ATTEMPT = CHALLENGES_WITH_ID + "/attempt";
     public static final String CHALLENGE_STREAM = CHALLENGES_WITH_ID + "/stream";
}
