package com.playground.moviehub.utils;

import java.util.Objects;

public final class PatchUtils {

    private PatchUtils() {
        // Private constructor to prevent instantiation of this utility class.
    }

    public static <T> T getOrDefault(T newValue, T defaultValue) {
        return Objects.nonNull(newValue) ? newValue : defaultValue;
    }
}