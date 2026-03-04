package com.et.taro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cwa")
public class CwaProperties {

    private String apiKey;
    private String baseUrl;
}
