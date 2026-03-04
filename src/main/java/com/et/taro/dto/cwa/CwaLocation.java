package com.et.taro.dto.cwa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CwaLocation {

    @JsonProperty("LocationName")
    private String locationName;

    @JsonProperty("Geocode")
    private String geocode;

    @JsonProperty("Latitude")
    private String latitude;

    @JsonProperty("Longitude")
    private String longitude;

    @JsonProperty("WeatherElement")
    private List<CwaWeatherElement> weatherElement;
}
