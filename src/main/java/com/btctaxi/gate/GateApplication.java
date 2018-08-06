package com.btctaxi.gate;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import genesis.gate.config.DistConfig;
import genesis.gate.config.KycConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class, DataSourceAutoConfiguration.class}, scanBasePackages = "genesis")
@EnableAsync
@EnableRetry
public class GateApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(GateApplication.class, args);
        ctx.registerShutdownHook();
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("TBSESSION");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("(?:^.+?\\.)?([A-Za-z0-9_-]+\\.[a-z]+)$");
        return serializer;
    }

    @Bean
    AmazonS3 amazonS3(KycConfig kycConfig) {
        AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(kycConfig.getKey(), kycConfig.getSecret()));
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion(Regions.fromName(kycConfig.getRegion())).build();
        return s3;
    }

    @Bean
    public RestTemplate http(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    ConfigureRedisAction redisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    @Bean
    TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean("error")
    MessageSource errorMessage() {
        ResourceBundleMessageSource errors = new ResourceBundleMessageSource();
        errors.setBasename("i18n/error");
        errors.setDefaultEncoding("UTF-8");
        return errors;
    }

    @Bean("email.title")
    MessageSource emailTitle(DistConfig distConfig) {
        ResourceBundleMessageSource errors = new ResourceBundleMessageSource();
        errors.setBasename("i18n/email/" + distConfig.getName() + "/title");
        errors.setDefaultEncoding("UTF-8");
        return errors;
    }
}
