package com.btctaxi.gate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "s3.kyc")
public class KycConfig {
    private String region;
    private String key;
    private String secret;
    private String bucket;
}
