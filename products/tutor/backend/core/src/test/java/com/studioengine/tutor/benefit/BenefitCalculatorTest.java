package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.enums.BenefitType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BenefitCalculatorTest {

    @ParameterizedTest
    @MethodSource("calculations")
    void shouldCalculateFinalPrice(BigDecimal originalPrice, BenefitType type, BigDecimal value, BigDecimal expectedPrice) {
        var result = BenefitCalculator.calculateFinalPrice(originalPrice, type, value);
        assertThat(result).isEqualByComparingTo(expectedPrice);
    }

    private static Stream<Arguments> calculations() {
        return Stream.of(
                // FREE_LESSON → price becomes 0
                Arguments.of(BigDecimal.valueOf(100), BenefitType.FREE_LESSON, null, BigDecimal.ZERO),
                // FIXED_AMOUNT_OFF → subtract value
                Arguments.of(BigDecimal.valueOf(100), BenefitType.FIXED_AMOUNT_OFF, BigDecimal.valueOf(30), BigDecimal.valueOf(70)),
                // PERCENTAGE_DISCOUNT → subtract percentage
                Arguments.of(BigDecimal.valueOf(100), BenefitType.PERCENTAGE_DISCOUNT, BigDecimal.valueOf(10), BigDecimal.valueOf(90)),
                // discount exceeds price → should not go negative
                Arguments.of(BigDecimal.valueOf(20), BenefitType.FIXED_AMOUNT_OFF, BigDecimal.valueOf(50), BigDecimal.ZERO),
                // 100% discount
                Arguments.of(BigDecimal.valueOf(80), BenefitType.PERCENTAGE_DISCOUNT, BigDecimal.valueOf(100), BigDecimal.ZERO)
        );
    }
}