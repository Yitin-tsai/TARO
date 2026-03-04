package com.et.taro.service;

import com.et.taro.client.AirQualityClient;
import com.et.taro.dto.AirQualityResponse;
import com.et.taro.dto.AqiStationRaw;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final AirQualityClient airQualityClient;
    private final GeoService geoService;

    public AirQualityResponse getAirQuality(double lat, double lng) {
        List<AqiStationRaw> stations = airQualityClient.fetchAllStations();

        AqiStationRaw nearest = stations.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .min(Comparator.comparingDouble(s ->
                        geoService.calculateDistanceMeters(
                                lat, lng,
                                parseDoubleSafe(s.getLatitude()),
                                parseDoubleSafe(s.getLongitude()))))
                .orElseThrow(() -> new RuntimeException("No AQI data available"));

        return mapToResponse(nearest);
    }

    private AirQualityResponse mapToResponse(AqiStationRaw raw) {
        return AirQualityResponse.builder()
                .aqi(parseIntSafe(raw.getAqi()))
                .status(raw.getStatus())
                .pm25(parseDoubleSafe(raw.getPm25()))
                .pm10(parseDoubleSafe(raw.getPm10()))
                .o3(parseDoubleSafe(raw.getO3()))
                .co(parseDoubleSafe(raw.getCo()))
                .mainPollutant(raw.getPollutant())
                .station(raw.getSitename())
                .county(raw.getCounty())
                .updatedAt(raw.getPublishtime())
                .build();
    }

    private int parseIntSafe(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
