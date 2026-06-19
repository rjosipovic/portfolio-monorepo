package com.studioengine.tutor.payment.provider;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProviderSession {

    String sessionId;
    String redirectUrl;
}
