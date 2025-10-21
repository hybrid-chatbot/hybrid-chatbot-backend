package com.example.navershopping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "naver.shopping")
@Data
public class NaverConfig {
    private String clientId;
    private String clientSecret;
}