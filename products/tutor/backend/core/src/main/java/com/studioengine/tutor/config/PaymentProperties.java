package com.studioengine.tutor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private String stripeSecretKey;
    private String stripeWebhookSecret;
    private String stripeSuccessUrl;
    private Duration stripeSessionExpiry;
    private String stripeCancelUrl;
    private String bankIban;
    private String bankModel;
    private String bankRecipientName;
}
