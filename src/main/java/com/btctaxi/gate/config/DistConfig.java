package com.btctaxi.gate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dist")
public class DistConfig {
    private String name;
    private String emailSender;
    private String smsSign;
    private String zendeskPrefix;
    private String rateUrl;
}
