package com.et.taro.client;

import com.et.taro.config.CwaProperties;
import com.et.taro.dto.cwa.CwaLocation;
import com.et.taro.dto.cwa.CwaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherClient {

    private final WebClient cwaWebClient;
    private final CwaProperties cwaProperties;

    @Cacheable("weather")
    public List<CwaLocation> fetchTaipeiWeather() {
        log.info("Fetching weather data from CWA API (cache miss)");

        CwaResponse response = cwaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/F-D0047-063")
                        .queryParam("Authorization", cwaProperties.getApiKey())
                        .queryParam("format", "JSON")
                        .build())
                .retrieve()
                .bodyToMono(CwaResponse.class)
                .block();

        if (response == null || response.getRecords() == null
                || response.getRecords().getLocations() == null
                || response.getRecords().getLocations().isEmpty()) {
            log.warn("CWA API returned empty response");
            return List.of();
        }

        List<CwaLocation> locations = response.getRecords()
                .getLocations().get(0).getLocation();

        log.info("Fetched weather data for {} districts", locations.size());
        return locations;
    }
}
