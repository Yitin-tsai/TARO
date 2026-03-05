package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TdxBusStop {

    @JsonProperty("StopID")
    private String stopId;

    @JsonProperty("StopName")
    private TdxLocalized stopName;

    @JsonProperty("StopPosition")
    private Position stopPosition;

    @JsonProperty("StationID")
    private String stationId;

    @JsonProperty("RouteID")
    private String routeId;

    @JsonProperty("RouteName")
    private TdxLocalized routeName;

    @JsonProperty("Direction")
    private int direction;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        @JsonProperty("PositionLon")
        private double positionLon;

        @JsonProperty("PositionLat")
        private double positionLat;
    }
}
