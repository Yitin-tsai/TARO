package com.et.taro.client;

import com.et.taro.config.YouBikeProperties;
import com.et.taro.dto.YouBikeStationRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class YouBikeClient {

    private final WebClient youBikeWebClient;
    private final YouBikeProperties youBikeProperties;

    @Cacheable("youbikeStations")
    public List<YouBikeStationRaw> fetchAllStations() {
        log.info("Fetching YouBike stations from APIs (cache miss)");

        List<YouBikeStationRaw> taipeiStations = fetchFrom(
                youBikeProperties.getApi().getTaipeiUrl(), "Taipei");
        List<YouBikeStationRaw> newTaipeiStations = fetchFrom(
                youBikeProperties.getApi().getNewTaipeiUrl(), "NewTaipei");

        List<YouBikeStationRaw> all = new ArrayList<>(taipeiStations.size() + newTaipeiStations.size());
        all.addAll(taipeiStations);
        all.addAll(newTaipeiStations);

        log.info("Fetched {} YouBike stations total (Taipei={}, NewTaipei={})",
                all.size(), taipeiStations.size(), newTaipeiStations.size());
        return all;
    }

    private List<YouBikeStationRaw> fetchFrom(String url, String city) {
        try {
            List<YouBikeStationRaw> stations = youBikeWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<YouBikeStationRaw>>() {})
                    .block();

            if (stations == null) {
                log.warn("YouBike {} API returned null", city);
                return List.of();
            }
            return stations;
        } catch (Exception e) {
            log.error("Failed to fetch YouBike stations from {}", city, e);
            return List.of();
        }
    }
}
