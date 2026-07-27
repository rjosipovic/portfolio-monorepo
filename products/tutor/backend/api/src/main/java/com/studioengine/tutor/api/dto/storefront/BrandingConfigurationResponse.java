package com.studioengine.tutor.api.dto.storefront;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = BrandingConfigurationResponse.BrandingConfigurationResponseBuilder.class)
public class BrandingConfigurationResponse {

    String name;
    String logoUrl;
    String primaryColor;
    String locale;
    String currency;
    String timezone;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BrandingConfigurationResponseBuilder {}
}
