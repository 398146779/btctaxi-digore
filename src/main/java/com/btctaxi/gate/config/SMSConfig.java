package com.btctaxi.gate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sms")
public class SMSConfig {
    private String amazonKey;
    private String amazonSecret;
    private String twilioUrl;
    private String twilioFrom;
    private String twilioAuthorization;
    private String yunpianUrl;
    private String yunpianKey;
}
