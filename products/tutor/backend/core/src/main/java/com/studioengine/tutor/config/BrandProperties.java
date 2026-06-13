package com.studioengine.tutor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.brand")
public class BrandProperties {

    private String name;
    private String logoUrl;
    private String primaryColor;
    private String locale;
    private String currency;
    private String timezone;
}
