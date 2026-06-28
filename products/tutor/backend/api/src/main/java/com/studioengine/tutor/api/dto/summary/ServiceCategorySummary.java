package com.studioengine.tutor.api.dto.summary;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ServiceCategorySummary.ServiceCategorySummaryBuilder.class)
public class ServiceCategorySummary {

    UUID id;
    String name;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ServiceCategorySummaryBuilder {}
}
