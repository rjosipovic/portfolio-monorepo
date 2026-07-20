package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.enums.BenefitType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BenefitCalculator {

    public static BigDecimal calculateFinalPrice(
            BigDecimal originalPrice,
            BenefitType benefitType,
            BigDecimal benefitValue
    ) {
        return switch (benefitType) {
            case FREE_LESSON -> BigDecimal.ZERO;
            case FIXED_AMOUNT_OFF -> originalPrice.subtract(benefitValue).max(BigDecimal.ZERO);
            case PERCENTAGE_DISCOUNT -> originalPrice.multiply(BigDecimal.ONE.subtract(benefitValue.divide(BigDecimal.valueOf(100),4, RoundingMode.HALF_UP)));
        };
    }
}
