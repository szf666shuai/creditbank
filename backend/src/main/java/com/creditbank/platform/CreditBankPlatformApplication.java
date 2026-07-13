package com.creditbank.platform;

import com.creditbank.platform.config.LlmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LlmProperties.class)
public class CreditBankPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditBankPlatformApplication.class, args);
    }
}
