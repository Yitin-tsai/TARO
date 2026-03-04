package com.et.taro.client;

import com.et.taro.config.MoenvProperties;
import com.et.taro.dto.AqiStationRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AirQualityClient {

    private final WebClient moenvWebClient;
    private final MoenvProperties moenvProperties;

    @Cacheable("airQuality")
    public List<AqiStationRaw> fetchAllStations() {
        log.info("Fetching AQI data from MOENV API (cache miss)");

        List<AqiStationRaw> stations = moenvWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/aqx_p_432")
                        .queryParam("api_key", moenvProperties.getApiKey())
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AqiStationRaw>>() {})
                .block();

        if (stations == null) {
            log.warn("MOENV API returned null");
            return List.of();
        }

        log.info("Fetched {} AQI stations", stations.size());
        return stations;
    }
}
