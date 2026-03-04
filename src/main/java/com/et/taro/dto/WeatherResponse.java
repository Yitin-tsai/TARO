package com.et.taro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private int temp;
    private int feelsLike;
    private String desc;
    private int rainProb;
    private int humidity;
    private double windSpeed;
    private String windDir;
    private int uvIndex;
    private String district;
    private String updatedAt;
}
