package com.et.taro.client;

import com.et.taro.config.TdxProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TdxAuthClient {

    private final WebClient tdxAuthWebClient;
    private final TdxProperties tdxProperties;

    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public synchronized String getToken() {
        if (Instant.now().isAfter(tokenExpiry.minusSeconds(300))) {
            refreshToken();
        }
        return accessToken;
    }

    private void refreshToken() {
        log.info("Refreshing TDX OAuth2 token");

        @SuppressWarnings("unchecked")
        Map<String, Object> response = tdxAuthWebClient.post()
                .uri(tdxProperties.getAuthUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", tdxProperties.getClientId())
                        .with("client_secret", tdxProperties.getClientSecret()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Failed to obtain TDX token");
        }

        this.accessToken = (String) response.get("access_token");
        int expiresIn = (Integer) response.get("expires_in");
        this.tokenExpiry = Instant.now().plusSeconds(expiresIn);

        log.info("TDX token refreshed, expires in {} seconds", expiresIn);
    }
}
