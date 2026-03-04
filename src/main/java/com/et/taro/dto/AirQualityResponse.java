package com.et.taro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityResponse {
    private int aqi;
    private String status;
    private double pm25;
    private double pm10;
    private double o3;
    private double co;
    private String mainPollutant;
    private String station;
    private String county;
    private String updatedAt;
}
