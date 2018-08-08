package com.btctaxi.gate.vendor;

import com.btctaxi.gate.config.SMSConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class Twilio {
    private RestTemplate http;
    private SMSConfig smsConfig;

    public Twilio(RestTemplate http, SMSConfig smsConfig) {
        this.http = http;
        this.smsConfig = smsConfig;
    }

    public void send(String mobile, String content) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", smsConfig.getTwilioAuthorization());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("From", smsConfig.getTwilioFrom());
        params.add("To", mobile);
        params.add("Body", content);

        HttpEntity<?> req = new HttpEntity<>(params, headers);

        http.postForEntity(smsConfig.getTwilioUrl(), req, String.class);
    }
}
