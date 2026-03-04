package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TdxMetroStation {

    @JsonProperty("StationID")
    private String stationId;

    @JsonProperty("StationName")
    private TdxLocalized stationName;

    @JsonProperty("StationPosition")
    private Position stationPosition;

    @Data
    public static class Position {

        @JsonProperty("PositionLon")
        private double positionLon;

        @JsonProperty("PositionLat")
        private double positionLat;
    }
}
