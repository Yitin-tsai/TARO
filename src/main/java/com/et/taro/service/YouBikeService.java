package com.et.taro.service;

import com.et.taro.client.YouBikeClient;
import com.et.taro.config.YouBikeProperties;
import com.et.taro.dto.YouBikeStationRaw;
import com.et.taro.dto.YouBikeStationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouBikeService {

    private final YouBikeClient youBikeClient;
    private final GeoService geoService;
    private final YouBikeProperties youBikeProperties;

    public List<YouBikeStationResponse> findNearby(double lat, double lng, Integer radiusMeters) {
        int radius = (radiusMeters != null && radiusMeters > 0) ? radiusMeters : youBikeProperties.getDefaultRadius();

        List<YouBikeStationRaw> allStations = youBikeClient.fetchAllStations();

        return allStations.stream()
                .filter(s -> "1".equals(s.getAct()))
                .map(station -> {
                    double distance = geoService.calculateDistanceMeters(
                            lat, lng, station.getLatitude(), station.getLongitude());
                    return Map.entry(station, distance);
                })
                .filter(entry -> entry.getValue() <= radius)
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(entry -> mapToResponse(entry.getKey(), (int) Math.round(entry.getValue())))
                .collect(Collectors.toList());
    }

    private YouBikeStationResponse mapToResponse(YouBikeStationRaw raw, int distanceMeters) {
        return YouBikeStationResponse.builder()
                .stationNo(raw.getSno())
                .name(cleanStationName(raw.getSna()))
                .district(raw.getSarea())
                .address(raw.getAr())
                .totalSlots(raw.getQuantity())
                .availableBikes(raw.getAvailableRentBikes())
                .emptySlots(raw.getAvailableReturnBikes())
                .lat(raw.getLatitude())
                .lng(raw.getLongitude())
                .distanceMeters(distanceMeters)
                .updatedAt(raw.getMday())
                .build();
    }

    private String cleanStationName(String sna) {
        if (sna == null) return "";
        return sna.replaceFirst("^YouBike2\\.0_", "");
    }
}
