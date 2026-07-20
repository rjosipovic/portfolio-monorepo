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
public class BenefitApplication {

    UUID benefitId;       // identifier of the StudentBenefit entity to consume later
    BenefitType type;
    BigDecimal originalPrice;
    BigDecimal finalPrice;
    boolean applied;              // false if no benefit was available

    public static BenefitApplication none(BigDecimal originalPrice) {
        return BenefitApplication.builder()
                .benefitId(null)
                .type(null)
                .originalPrice(originalPrice)
                .finalPrice(originalPrice)
                .applied(false)
                .build();
    }
}

