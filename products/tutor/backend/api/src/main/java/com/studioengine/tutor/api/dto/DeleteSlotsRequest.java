package com.studioengine.tutor.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = DeleteSlotsRequest.DeleteSlotsRequestBuilder.class)
public class DeleteSlotsRequest {

    @NotEmpty
    List<UUID> slotIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DeleteSlotsRequestBuilder {}
}
