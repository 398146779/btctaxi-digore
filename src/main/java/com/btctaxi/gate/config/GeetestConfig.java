package com.btctaxi.gate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "geetest")
public class GeetestConfig {
    private String id;
    private String key;
    private boolean newFailBack;

    private String ocrId;
    private String ocrKey;

    private String ocrImgId;
    private String ocrImgKey;
}
