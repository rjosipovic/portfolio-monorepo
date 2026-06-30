package com.studioengine.tutor.catalog;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AvailableService {

    UUID id;
    String name;
    String description;
    BigDecimal price;
    String currency;
}
