package com.studioengine.tutor.api.dto.storefront;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ServiceCategoryResponse.ServiceCategoryResponseBuilder.class)
public class ServiceCategoryResponse {

    UUID id;
    String name;
    String description;
    BigDecimal price;
    String currency;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ServiceCategoryResponseBuilder {}
}
