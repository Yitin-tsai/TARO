package com.et.taro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tdx")
public class TdxProperties {

    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String authUrl;
}
