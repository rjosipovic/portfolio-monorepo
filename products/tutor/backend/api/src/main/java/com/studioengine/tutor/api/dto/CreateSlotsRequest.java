package com.studioengine.tutor.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = CreateSlotsRequest.CreateSlotsRequestBuilder.class)
public class CreateSlotsRequest {

    @NotEmpty
    @Valid
    List<SlotDefinition> slots;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonDeserialize(builder = SlotDefinition.SlotDefinitionBuilder.class)
    public static class SlotDefinition {

        @NotNull
        LocalDate date;

        @NotNull
        LocalTime startTime;

        @JsonPOJOBuilder(withPrefix = "")
        public static class SlotDefinitionBuilder {}
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateSlotsRequestBuilder {}
}
