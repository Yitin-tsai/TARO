package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TdxStationExit {

    @JsonProperty("StationID")
    private String stationId;

    @JsonProperty("StationName")
    private TdxLocalized stationName;

    @JsonProperty("ExitID")
    private String exitId;

    @JsonProperty("ExitName")
    private TdxLocalized exitName;

    @JsonProperty("ExitPosition")
    private Position exitPosition;

    @JsonProperty("LocationDescription")
    private String locationDescription;

    @JsonProperty("Elevator")
    private boolean elevator;

    @Data
    public static class Position {

        @JsonProperty("PositionLon")
        private double positionLon;

        @JsonProperty("PositionLat")
        private double positionLat;
    }
}
