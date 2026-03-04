package com.et.taro.service;

import com.et.taro.client.WeatherClient;
import com.et.taro.dto.WeatherResponse;
import com.et.taro.dto.cwa.CwaLocation;
import com.et.taro.dto.cwa.CwaTimeEntry;
import com.et.taro.dto.cwa.CwaWeatherElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;
    private final GeoService geoService;

    private static final DateTimeFormatter CWA_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public WeatherResponse getWeather(double lat, double lng) {
        List<CwaLocation> locations = weatherClient.fetchTaipeiWeather();

        // 找最近的行政區
        CwaLocation nearest = locations.stream()
                .min(Comparator.comparingDouble(loc ->
                        geoService.calculateDistanceMeters(
                                lat, lng,
                                Double.parseDouble(loc.getLatitude()),
                                Double.parseDouble(loc.getLongitude()))))
                .orElseThrow(() -> new RuntimeException("No weather data available"));

        return extractWeatherResponse(nearest);
    }

    private WeatherResponse extractWeatherResponse(CwaLocation location) {
        LocalDateTime now = LocalDateTime.now();

        WeatherResponse.WeatherResponseBuilder builder = WeatherResponse.builder()
                .district(location.getLocationName());

        for (CwaWeatherElement element : location.getWeatherElement()) {
            CwaTimeEntry current = findCurrentTimeEntry(element.getTime(), now);
            if (current == null) continue;

            Map<String, String> values = mergeElementValues(current.getElementValue());

            switch (element.getElementName()) {
                case "平均溫度" -> builder.temp(parseIntSafe(values.get("Temperature")));
                case "最高體感溫度" -> builder.feelsLike(parseIntSafe(values.get("MaxApparentTemperature")));
                case "天氣現象" -> builder.desc(values.get("Weather"));
                case "12小時降雨機率" -> builder.rainProb(parseIntSafe(values.get("ProbabilityOfPrecipitation")));
                case "平均相對濕度" -> builder.humidity(parseIntSafe(values.get("RelativeHumidity")));
                case "風速" -> builder.windSpeed(parseDoubleSafe(values.get("WindSpeed")));
                case "風向" -> builder.windDir(values.get("WindDirection"));
                case "紫外線指數" -> builder.uvIndex(parseIntSafe(values.get("UVIndex")));
            }

            builder.updatedAt(current.getStartTime());
        }

        return builder.build();
    }

    private CwaTimeEntry findCurrentTimeEntry(List<CwaTimeEntry> timeEntries, LocalDateTime now) {
        for (CwaTimeEntry entry : timeEntries) {
            LocalDateTime start = LocalDateTime.parse(entry.getStartTime(), CWA_TIME_FORMAT);
            LocalDateTime end = LocalDateTime.parse(entry.getEndTime(), CWA_TIME_FORMAT);
            if (!now.isBefore(start) && now.isBefore(end)) {
                return entry;
            }
        }
        // 找不到當前時段就用第一筆（最近的未來時段）
        return timeEntries.isEmpty() ? null : timeEntries.get(0);
    }

    private Map<String, String> mergeElementValues(List<Map<String, String>> elementValues) {
        if (elementValues == null || elementValues.isEmpty()) return Map.of();
        if (elementValues.size() == 1) return elementValues.get(0);
        // 多個 map 合併成一個
        var merged = new java.util.HashMap<String, String>();
        for (Map<String, String> map : elementValues) {
            merged.putAll(map);
        }
        return merged;
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
