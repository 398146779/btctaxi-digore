package com.btctaxi.gate.vendor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import genesis.gate.config.SMSConfig;
import org.springframework.stereotype.Component;

@Component
public class AmazonSMS {
    private SMSConfig smsConfig;

    public AmazonSMS(SMSConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    public void send(String mobile, String content) {
        AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(smsConfig.getAmazonKey(), smsConfig.getAmazonSecret()));
        AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
        AmazonSNS sns = builder.withRegion(Regions.AP_NORTHEAST_1).withCredentials(provider).build();
        PublishResult result = sns.publish(new PublishRequest().withMessage(content).withPhoneNumber(mobile));
    }
}
