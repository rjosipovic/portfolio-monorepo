package com.playground.moviehub.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// The single source of truth for the API's base path.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiPaths {

    private static final String API_BASE = "/v1";

    public static final String ACTORS = API_BASE + "/actors";
    public static final String ACTORS_WITH_ID = ACTORS + "/{id}";

    public static final String DIRECTORS = API_BASE + "/directors";
    public static final String DIRECTORS_WITH_ID = DIRECTORS + "/{id}";

    public static final String MOVIES = API_BASE + "/movies";
    public static final String MOVIES_WITH_ID = MOVIES + "/{id}";

    public static final String CHARACTERS = API_BASE + "/characters";
    public static final String CHARACTERS_WITH_ID = CHARACTERS + "/{id}";
}