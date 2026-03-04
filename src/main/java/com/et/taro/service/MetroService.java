package com.et.taro.service;

import com.et.taro.client.MetroClient;
import com.et.taro.dto.MetroResponse;
import com.et.taro.dto.tdx.TdxMetroStation;
import com.et.taro.dto.tdx.TdxServiceDay;
import com.et.taro.dto.tdx.TdxStationExit;
import com.et.taro.dto.tdx.TdxStationTimeTable;
import com.et.taro.dto.tdx.TdxTimetableEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetroService {

    private static final double MAX_DISTANCE_METERS = 1000.0;
    private static final int MAX_TRAINS_PER_DIRECTION = 3;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<String, String> LINE_COLORS = Map.of(
            "BR", "#C48C31",
            "R", "#E3002C",
            "G", "#1A803F",
            "O", "#F8B61C",
            "BL", "#0070BD",
            "Y", "#FFDB00"
    );

    private static final Map<String, String> LINE_NAMES = Map.of(
            "BR", "文湖線",
            "R", "淡水信義線",
            "G", "松山新店線",
            "O", "中和新蘆線",
            "BL", "板南線",
            "Y", "環狀線"
    );

    private final MetroClient metroClient;
    private final GeoService geoService;

    public MetroResponse getNearbyArrivals(double lat, double lng) {
        List<TdxMetroStation> allStations = metroClient.fetchAllStations();

        // 找最近的捷運站
        TdxMetroStation nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (TdxMetroStation station : allStations) {
            double dist = geoService.calculateDistanceMeters(
                    lat, lng,
                    station.getStationPosition().getPositionLat(),
                    station.getStationPosition().getPositionLon());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = station;
            }
        }

        if (nearest == null || minDistance > MAX_DISTANCE_METERS) {
            return MetroResponse.builder()
                    .station("附近無捷運站")
                    .distanceMeters(0)
                    .directions(List.of())
                    .build();
        }

        // 找同一物理位置的所有站點（轉乘站如台北車站有 BL12 和 R10）
        double nearestLat = nearest.getStationPosition().getPositionLat();
        double nearestLon = nearest.getStationPosition().getPositionLon();
        List<String> stationIds = allStations.stream()
                .filter(s -> geoService.calculateDistanceMeters(
                        nearestLat, nearestLon,
                        s.getStationPosition().getPositionLat(),
                        s.getStationPosition().getPositionLon()) < 50)
                .map(TdxMetroStation::getStationId)
                .toList();

        // 找最近出入口
        MetroResponse.NearestExit nearestExit = findNearestExit(lat, lng, stationIds);

        // 從時刻表計算下一班車，按路線+方向分組
        List<MetroResponse.DirectionArrival> directions = buildDirectionArrivals(stationIds);

        return MetroResponse.builder()
                .station(nearest.getStationName().getZhTw())
                .distanceMeters(Math.round(minDistance))
                .nearestExit(nearestExit)
                .directions(directions)
                .build();
    }

    private MetroResponse.NearestExit findNearestExit(double lat, double lng, List<String> stationIds) {
        List<TdxStationExit> allExits = metroClient.fetchAllExits();

        TdxStationExit closest = null;
        double closestDist = Double.MAX_VALUE;

        for (TdxStationExit exit : allExits) {
            if (!stationIds.contains(exit.getStationId())) {
                continue;
            }
            double dist = geoService.calculateDistanceMeters(
                    lat, lng,
                    exit.getExitPosition().getPositionLat(),
                    exit.getExitPosition().getPositionLon());
            if (dist < closestDist) {
                closestDist = dist;
                closest = exit;
            }
        }

        if (closest == null) {
            return null;
        }

        return MetroResponse.NearestExit.builder()
                .exitName(closest.getExitName().getZhTw())
                .locationDescription(closest.getLocationDescription())
                .distanceMeters(Math.round(closestDist))
                .elevator(closest.isElevator())
                .build();
    }

    private List<MetroResponse.DirectionArrival> buildDirectionArrivals(List<String> stationIds) {
        List<TdxStationTimeTable> allTimeTables = metroClient.fetchAllTimeTables();
        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        // 按 lineId + direction 分組，收集所有到站時間
        // key = "BL-0" or "R-1"
        Map<String, List<MetroResponse.TrainInfo>> grouped = new LinkedHashMap<>();

        for (TdxStationTimeTable tt : allTimeTables) {
            if (!stationIds.contains(tt.getStationId())) {
                continue;
            }
            if (!matchesServiceDay(tt.getServiceDay(), today)) {
                continue;
            }

            String groupKey = tt.getLineId() + "-" + tt.getDirection();

            for (TdxTimetableEntry entry : tt.getTimetables()) {
                LocalTime arrivalTime = LocalTime.parse(entry.getArrivalTime(), TIME_FORMAT);
                if (arrivalTime.isAfter(now)) {
                    int waitMin = (int) java.time.Duration.between(now, arrivalTime).toMinutes();
                    grouped.computeIfAbsent(groupKey, k -> new ArrayList<>())
                            .add(MetroResponse.TrainInfo.builder()
                                    .destination(tt.getDestinationStationName().getZhTw())
                                    .arrivalTime(entry.getArrivalTime())
                                    .waitMin(waitMin)
                                    .build());
                    break; // 每個時刻表只取下一班
                }
            }
        }

        // 每個方向排序取前 N 班
        List<MetroResponse.DirectionArrival> result = new ArrayList<>();
        for (Map.Entry<String, List<MetroResponse.TrainInfo>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("-");
            String lineId = parts[0];
            int direction = Integer.parseInt(parts[1]);

            List<MetroResponse.TrainInfo> trains = entry.getValue().stream()
                    .filter(t -> t.getWaitMin() <= 30)
                    .sorted(Comparator.comparingInt(MetroResponse.TrainInfo::getWaitMin))
                    .limit(MAX_TRAINS_PER_DIRECTION)
                    .toList();

            if (trains.isEmpty()) {
                continue;
            }

            result.add(MetroResponse.DirectionArrival.builder()
                    .lineId(lineId)
                    .lineName(LINE_NAMES.getOrDefault(lineId, lineId))
                    .color(LINE_COLORS.getOrDefault(lineId, "#999999"))
                    .direction(direction)
                    .trains(trains)
                    .build());
        }

        // 按路線排序
        result.sort(Comparator.comparing(MetroResponse.DirectionArrival::getLineId)
                .thenComparingInt(MetroResponse.DirectionArrival::getDirection));

        return result;
    }

    private boolean matchesServiceDay(TdxServiceDay serviceDay, DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> serviceDay.isMonday();
            case TUESDAY -> serviceDay.isTuesday();
            case WEDNESDAY -> serviceDay.isWednesday();
            case THURSDAY -> serviceDay.isThursday();
            case FRIDAY -> serviceDay.isFriday();
            case SATURDAY -> serviceDay.isSaturday();
            case SUNDAY -> serviceDay.isSunday();
        };
    }
}
