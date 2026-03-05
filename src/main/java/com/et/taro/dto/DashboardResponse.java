package com.et.taro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private WeatherResponse weather;
    private AirQualityResponse airQuality;
    private MetroResponse metro;
    private BusResponse bus;
    private List<YouBikeStationResponse> youbike;
}
