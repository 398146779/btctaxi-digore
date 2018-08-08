package com.btctaxi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class, DataSourceAutoConfiguration.class}, scanBasePackages = "com")
@EnableAsync
@EnableAutoConfiguration
//@EnableEurekaServer
//@EnableDiscoveryClient
@EnableSwagger2
@Configuration
@MapperScan("com.btctaxi.domain")
public class AccountingApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(AccountingApplication.class, args);
        ctx.registerShutdownHook();
    }

//    @Bean
//    RestTemplate http(RestTemplateBuilder builder) {
//        return builder.build();
//    }


//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.example.controller"))
//                .paths(PathSelectors.ant("/foos/*"))
//                .build() ;
//    }
}
