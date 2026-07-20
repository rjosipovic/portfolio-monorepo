package com.studioengine.tutor.api.dto.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = BenefitResponse.BenefitResponseBuilder.class)
public class BenefitResponse {

    UUID id;
    String type;
    BigDecimal value;
    String note;
    boolean consumed;
    OffsetDateTime grantedAt;
    OffsetDateTime consumedAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BenefitResponseBuilder {}
}
