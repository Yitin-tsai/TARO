package com.et.taro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AqiStationRaw {
    private String siteid;
    private String sitename;
    private String county;
    private String aqi;
    private String status;
    private String pollutant;
    private String so2;
    private String co;
    private String o3;
    @JsonProperty("o3_8hr")
    private String o3_8hr;
    private String pm10;
    @JsonProperty("pm2.5")
    private String pm25;
    private String no2;
    @JsonProperty("wind_speed")
    private String windSpeed;
    @JsonProperty("wind_direc")
    private String windDirec;
    private String latitude;
    private String longitude;
    private String publishtime;
}
