package com.btctaxi.gate.vendor;

import genesis.gate.config.SMSConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class YunPian {
    private RestTemplate http;
    private SMSConfig smsConfig;

    public YunPian(RestTemplate http, SMSConfig smsConfig) {
        this.http = http;
        this.smsConfig = smsConfig;
    }

    public void send(int region_id, String phone, String content) {
        String mobile = phone;
        if (region_id != 86) {
            mobile = "%2B" + region_id + phone;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("apikey", smsConfig.getYunpianKey());
        params.add("text", content);
        params.add("mobile", mobile);

        http.postForEntity(smsConfig.getYunpianUrl(), params, String.class);
    }
}
