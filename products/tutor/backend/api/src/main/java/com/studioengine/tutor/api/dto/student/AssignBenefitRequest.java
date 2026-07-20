package com.studioengine.tutor.api.dto.student;

import com.studioengine.tutor.dataaccess.enums.BenefitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AssignBenefitRequest.AssignBenefitRequestBuilder.class)
public class AssignBenefitRequest {

    @NotNull
    BenefitType benefitType;

    @Positive
    BigDecimal value;

    String note;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AssignBenefitRequestBuilder {}
}
