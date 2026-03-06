package com.et.taro.service;

import com.et.taro.client.BusClient;
import com.et.taro.dto.BusResponse;
import com.et.taro.dto.tdx.TdxBusEstimate;
import com.et.taro.dto.tdx.TdxBusRoute;
import com.et.taro.dto.tdx.TdxBusStop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusService {

    private static final int DEFAULT_RADIUS = 300;
    private static final int MAX_STATIONS = 1;
    private static final String[] CITIES = {"Taipei", "NewTaipei"};

    private static final Map<Integer, String> STOP_STATUS_MAP = Map.of(
            0, "正常",
            1, "尚未發車",
            2, "交管不停靠",
            3, "末班駛離",
            4, "今日未營運"
    );

    private final BusClient busClient;
    private final GeoService geoService;

    public BusResponse getNearbyArrivals(double lat, double lng) {
        // 1. 從雙北取得附近站牌（空間查詢），記錄每個 stop 來自哪個城市
        Map<String, String> stopCityMap = new HashMap<>(); // stopId → city
        List<TdxBusStop> allStops = new ArrayList<>();
        for (String city : CITIES) {
            try {
                List<TdxBusStop> cityStops = busClient.fetchNearbyStops(city, lat, lng, DEFAULT_RADIUS);
                for (TdxBusStop stop : cityStops) {
                    if (stop.getStopId() != null) {
                        stopCityMap.put(stop.getStopId(), city);
                    }
                }
                allStops.addAll(cityStops);
            } catch (Exception e) {
                log.warn("Failed to fetch nearby bus stops for {}, continuing with other cities", city);
            }
        }

        if (allStops.isEmpty()) {
            return BusResponse.builder().stops(List.of()).build();
        }

        // 2. 按 StationID 分組，計算距離取最近的 N 個站
        Map<String, List<TdxBusStop>> byStation = allStops.stream()
                .filter(s -> s.getStationId() != null && s.getStopPosition() != null)
                .collect(Collectors.groupingBy(TdxBusStop::getStationId));

        record StationInfo(String stationId, String stationName, double distance,
                           Set<String> stopIds) {}

        List<StationInfo> nearestStations = byStation.entrySet().stream()
                .map(entry -> {
                    TdxBusStop first = entry.getValue().get(0);
                    double dist = geoService.calculateDistanceMeters(
                            lat, lng,
                            first.getStopPosition().getPositionLat(),
                            first.getStopPosition().getPositionLon());
                    String name = first.getStopName() != null ? first.getStopName().getZhTw() : "未知站名";
                    Set<String> stopIds = entry.getValue().stream()
                            .map(TdxBusStop::getStopId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    return new StationInfo(entry.getKey(), name, dist, stopIds);
                })
                .sorted(Comparator.comparingDouble(StationInfo::distance))
                .limit(MAX_STATIONS)
                .toList();

        if (nearestStations.isEmpty()) {
            return BusResponse.builder().stops(List.of()).build();
        }

        // 3. 按城市分組 StopID，只查對應城市的 ETA（避免浪費 API 額度）
        Map<String, List<String>> stopIdsByCity = new HashMap<>();
        for (StationInfo station : nearestStations) {
            for (String stopId : station.stopIds()) {
                String city = stopCityMap.get(stopId);
                if (city != null) {
                    stopIdsByCity.computeIfAbsent(city, k -> new ArrayList<>()).add(stopId);
                }
            }
        }

        List<TdxBusEstimate> allEstimates = new ArrayList<>();
        Map<String, TdxBusRoute> routeMap = new HashMap<>();
        for (String city : CITIES) {
            List<String> cityStopIds = stopIdsByCity.getOrDefault(city, List.of());
            if (!cityStopIds.isEmpty()) {
                try {
                    allEstimates.addAll(busClient.fetchEstimates(city, cityStopIds));
                } catch (Exception e) {
                    log.warn("Failed to fetch bus estimates for {}, continuing with partial data", city);
                }
                try {
                    for (TdxBusRoute route : busClient.fetchRoutes(city)) {
                        if (route.getRouteId() != null) {
                            routeMap.putIfAbsent(route.getRouteId(), route);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch bus routes for {}, continuing with partial data", city);
                }
            }
        }

        // 4. 按 StopID 分組 ETA
        Map<String, List<TdxBusEstimate>> estimatesByStop = allEstimates.stream()
                .filter(e -> e.getStopId() != null)
                .collect(Collectors.groupingBy(TdxBusEstimate::getStopId));

        // 5. 組合回應：每個站收集其 StopID 對應的 ETA
        List<BusResponse.NearbyStop> responseStops = new ArrayList<>();

        for (StationInfo station : nearestStations) {
            // 收集此站所有 StopID 的 ETA
            List<TdxBusEstimate> stationEstimates = new ArrayList<>();
            for (String stopId : station.stopIds()) {
                stationEstimates.addAll(estimatesByStop.getOrDefault(stopId, List.of()));
            }

            // 去重：同路線同方向只留一筆
            Map<String, TdxBusEstimate> uniqueRoutes = new LinkedHashMap<>();
            for (TdxBusEstimate est : stationEstimates) {
                String key = est.getRouteId() + "-" + est.getDirection();
                uniqueRoutes.putIfAbsent(key, est);
            }

            List<BusResponse.RouteArrival> routeArrivals = new ArrayList<>();
            for (TdxBusEstimate est : uniqueRoutes.values()) {
                Integer estimateMin = null;
                if (est.getEstimateTime() != null && est.getEstimateTime() >= 0) {
                    estimateMin = est.getEstimateTime() / 60;
                }
                String status = STOP_STATUS_MAP.getOrDefault(est.getStopStatus(), "未知");
                String routeName = est.getRouteName() != null ? est.getRouteName().getZhTw() : "未知路線";
                String destination = resolveDestination(est, routeMap);

                routeArrivals.add(BusResponse.RouteArrival.builder()
                        .routeName(routeName)
                        .direction(est.getDirection())
                        .destination(destination)
                        .estimateMin(estimateMin)
                        .status(status)
                        .build());
            }

            // 排序：有到站時間的優先，再按路線名
            routeArrivals.sort(Comparator
                    .comparing((BusResponse.RouteArrival r) -> r.getEstimateMin() == null ? 1 : 0)
                    .thenComparing(r -> !"正常".equals(r.getStatus()) ? 1 : 0)
                    .thenComparing(BusResponse.RouteArrival::getRouteName)
                    .thenComparingInt(BusResponse.RouteArrival::getDirection));

            responseStops.add(BusResponse.NearbyStop.builder()
                    .stationName(station.stationName())
                    .distanceMeters(Math.round(station.distance()))
                    .routes(routeArrivals)
                    .build());
        }

        return BusResponse.builder().stops(responseStops).build();
    }

    /**
     * 用路線資料解析目的地名稱
     * Direction 0（去程）→ 往 DestinationStopNameZh
     * Direction 1（返程）→ 往 DepartureStopNameZh
     */
    private String resolveDestination(TdxBusEstimate est, Map<String, TdxBusRoute> routeMap) {
        TdxBusRoute route = routeMap.get(est.getRouteId());
        if (route != null) {
            String dest = est.getDirection() == 0
                    ? route.getDestinationStopNameZh()
                    : route.getDepartureStopNameZh();
            if (dest != null && !dest.isBlank()) {
                return "往" + dest;
            }
        }
        return est.getDirection() == 0 ? "去程" : "返程";
    }
}
