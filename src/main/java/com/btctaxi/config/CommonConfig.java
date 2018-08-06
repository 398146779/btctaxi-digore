package com.btctaxi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "common")
public class CommonConfig {
    private String changeRateUrl;
    private BigDecimal withdrawQuota;
}
