package com.et.taro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "moenv")
public class MoenvProperties {

    private String apiKey;
    private String baseUrl;
}
