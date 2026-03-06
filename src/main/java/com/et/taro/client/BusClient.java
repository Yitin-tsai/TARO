package com.et.taro.client;

import com.et.taro.dto.tdx.TdxBusEstimate;
import com.et.taro.dto.tdx.TdxBusRoute;
import com.et.taro.dto.tdx.TdxBusStop;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusClient {

    private final WebClient tdxWebClient;
    private final TdxAuthClient tdxAuthClient;

    /**
     * 使用 TDX 空間查詢找附近公車站牌
     */
    @Retry(name = "tdx")
    @RateLimiter(name = "tdx")
    public List<TdxBusStop> fetchNearbyStops(String city, double lat, double lng, int radiusMeters) {
        String uri = String.format(
                "/v2/Bus/Stop/City/%s?$spatialFilter=nearby(%f,%f,%d)&$format=JSON",
                city, lat, lng, radiusMeters);

        log.info("Fetching nearby bus stops from TDX: city={}, radius={}m", city, radiusMeters);

        try {
            List<TdxBusStop> stops = tdxWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(tdxAuthClient.getToken()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TdxBusStop>>() {})
                    .block();

            if (stops == null) return List.of();
            log.info("Fetched {} bus stops for {}", stops.size(), city);
            return stops;
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("TDX rate limited for bus stops {}, will retry", city);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch bus stops for {}", city, e);
            return List.of();
        }
    }

    /**
     * 查詢即時到站時間，用 StopID 過濾
     */
    @Retry(name = "tdx")
    @RateLimiter(name = "tdx")
    public List<TdxBusEstimate> fetchEstimates(String city, List<String> stopIds) {
        if (stopIds.isEmpty()) return List.of();

        String filter = stopIds.stream()
                .map(id -> "StopID eq '" + id + "'")
                .collect(Collectors.joining(" or "));

        String uri = String.format(
                "/v2/Bus/EstimatedTimeOfArrival/City/%s?$filter=%s&$format=JSON",
                city, filter);

        log.info("Fetching bus ETA from TDX: city={}, stops={}", city, stopIds.size());

        try {
            List<TdxBusEstimate> estimates = tdxWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(tdxAuthClient.getToken()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TdxBusEstimate>>() {})
                    .block();

            if (estimates == null) return List.of();
            log.info("Fetched {} bus ETA entries for {}", estimates.size(), city);
            return estimates;
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("TDX rate limited for bus ETA {}, will retry", city);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch bus ETA for {}", city, e);
            return List.of();
        }
    }

    /**
     * 查詢城市所有公車路線起迄站名，快取 24 小時
     */
    @Retry(name = "tdx")
    @RateLimiter(name = "tdx")
    @Cacheable(value = "busRoutes", sync = true)
    public List<TdxBusRoute> fetchRoutes(String city) {
        String uri = String.format(
                "/v2/Bus/Route/City/%s?$select=RouteID,RouteName,DepartureStopNameZh,DestinationStopNameZh&$format=JSON",
                city);

        log.info("Fetching bus routes from TDX: city={}", city);

        try {
            List<TdxBusRoute> routes = tdxWebClient.get()
                    .uri(uri)
                    .headers(h -> h.setBearerAuth(tdxAuthClient.getToken()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TdxBusRoute>>() {})
                    .block();

            if (routes == null) return List.of();
            log.info("Fetched {} bus routes for {}", routes.size(), city);
            return routes;
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("TDX rate limited for bus routes {}, will retry", city);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch bus routes for {}", city, e);
            return List.of();
        }
    }
}
