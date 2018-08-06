package com.btctaxi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wallet")
public class WalletConfig {
    private String host;
    private String key;
    private String secret;
    private String eosAddress;

    private String queryUrl;
    private String allowTransfer;
    private String url5555;
    private String url5432;
}
