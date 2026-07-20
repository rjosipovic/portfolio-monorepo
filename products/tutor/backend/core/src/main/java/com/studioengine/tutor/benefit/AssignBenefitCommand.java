package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.enums.BenefitType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssignBenefitCommand {

    UUID studentId;
    BenefitType type;
    BigDecimal value;
    String note;
}