package com.playground.moviehub.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = Person.PersonBuilder.class)
public class Person {

    UUID id;
    String name;
    String bio;
    String fullBio;
    String imageUrl;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PersonBuilder {}
}
