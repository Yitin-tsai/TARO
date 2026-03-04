package com.et.taro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final YouBikeProperties youBikeProperties;
    private final CwaProperties cwaProperties;
    private final MoenvProperties moenvProperties;
    private final TdxProperties tdxProperties;

    @Bean
    public WebClient youBikeWebClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB — YouBike JSON ~3-4MB
                .build();
    }

    @Bean
    public WebClient cwaWebClient() {
        return WebClient.builder()
                .baseUrl(cwaProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // 5 MB
                .build();
    }

    @Bean
    public WebClient moenvWebClient() {
        return WebClient.builder()
                .baseUrl(moenvProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient tdxWebClient() {
        return WebClient.builder()
                .baseUrl(tdxProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB — StationTimeTable ~7MB
                .build();
    }

    @Bean
    public WebClient tdxAuthWebClient() {
        return WebClient.create();
    }
}
