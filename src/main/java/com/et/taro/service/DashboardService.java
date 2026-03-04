package com.et.taro.service;

import com.et.taro.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private static final int YOUBIKE_LIMIT = 5;

    private final WeatherService weatherService;
    private final AirQualityService airQualityService;
    private final MetroService metroService;
    private final YouBikeService youBikeService;

    public DashboardResponse getDashboard(double lat, double lng) {
        // 四個服務平行查詢
        CompletableFuture<WeatherResponse> weatherFuture = CompletableFuture
                .supplyAsync(() -> safeCall(() -> weatherService.getWeather(lat, lng), "weather"));

        CompletableFuture<AirQualityResponse> aqiFuture = CompletableFuture
                .supplyAsync(() -> safeCall(() -> airQualityService.getAirQuality(lat, lng), "airQuality"));

        CompletableFuture<MetroResponse> metroFuture = CompletableFuture
                .supplyAsync(() -> safeCall(() -> metroService.getNearbyArrivals(lat, lng), "metro"));

        CompletableFuture<List<YouBikeStationResponse>> youbikeFuture = CompletableFuture
                .supplyAsync(() -> safeCall(() -> {
                    List<YouBikeStationResponse> stations = youBikeService.findNearby(lat, lng, null);
                    return stations.size() > YOUBIKE_LIMIT ? stations.subList(0, YOUBIKE_LIMIT) : stations;
                }, "youbike"));
 
        CompletableFuture.allOf(weatherFuture, aqiFuture, metroFuture, youbikeFuture).join();

        return DashboardResponse.builder()
                .weather(weatherFuture.join())
                .airQuality(aqiFuture.join())
                .metro(metroFuture.join())
                .youbike(youbikeFuture.join())
                .build();
    }

    private <T> T safeCall(java.util.function.Supplier<T> supplier, String name) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Dashboard: {} query failed", name, e);
            return null;
        }
    }
}
