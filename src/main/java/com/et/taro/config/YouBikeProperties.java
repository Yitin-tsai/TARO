package com.et.taro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "youbike")
public class YouBikeProperties {

    private Api api = new Api();
    private int defaultRadius = 500;

    @Data
    public static class Api {
        private String taipeiUrl;
        private String newTaipeiUrl;
    }
}
