package com.gymplatform;

import com.gymplatform.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
public class GymPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymPlatformApplication.class, args);
    }
}
