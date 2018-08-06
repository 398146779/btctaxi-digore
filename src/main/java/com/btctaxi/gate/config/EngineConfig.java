package com.btctaxi.gate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "engine")
public class EngineConfig {
    private String messageHost;
    private int messagePort;
}
