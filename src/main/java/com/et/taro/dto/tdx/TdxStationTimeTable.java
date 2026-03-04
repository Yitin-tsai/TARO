package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TdxStationTimeTable {

    @JsonProperty("RouteID")
    private String routeId;

    @JsonProperty("LineID")
    private String lineId;

    @JsonProperty("StationID")
    private String stationId;

    @JsonProperty("StationName")
    private TdxLocalized stationName;

    @JsonProperty("Direction")
    private int direction;

    @JsonProperty("DestinationStaionID")
    private String destinationStationId;

    @JsonProperty("DestinationStationName")
    private TdxLocalized destinationStationName;

    @JsonProperty("Timetables")
    private List<TdxTimetableEntry> timetables;

    @JsonProperty("ServiceDay")
    private TdxServiceDay serviceDay;
}
