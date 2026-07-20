package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.enums.BenefitType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BenefitEntry {

    UUID id;
    BenefitType type;
    BigDecimal value;
    String note;
    boolean consumed;
    OffsetDateTime grantedAt;
    OffsetDateTime consumedAt;
}
