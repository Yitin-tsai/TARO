package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TdxMetroLiveBoard {

    @JsonProperty("StationID")
    private String stationId;

    @JsonProperty("StationName")
    private TdxLocalized stationName;

    @JsonProperty("LineID")
    private String lineId;

    @JsonProperty("LineName")
    private TdxLocalized lineName;

    @JsonProperty("TripHeadSign")
    private String tripHeadSign;

    @JsonProperty("DestinationStationID")
    private String destinationStationId;

    @JsonProperty("DestinationStationName")
    private TdxLocalized destinationStationName;

    @JsonProperty("EstimateTime")
    private Integer estimateTime;

    @JsonProperty("ServiceStatus")
    private Integer serviceStatus;
}
