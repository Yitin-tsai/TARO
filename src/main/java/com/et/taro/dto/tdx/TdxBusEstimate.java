package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TdxBusEstimate {

    @JsonProperty("StopID")
    private String stopId;

    @JsonProperty("StationID")
    private String stationId;

    @JsonProperty("RouteID")
    private String routeId;

    @JsonProperty("RouteName")
    private TdxLocalized routeName;

    @JsonProperty("Direction")
    private int direction;

    @JsonProperty("DestinationStopName")
    private TdxLocalized destinationStopName;

    @JsonProperty("EstimateTime")
    private Integer estimateTime;  // 秒，null = 無資料

    @JsonProperty("StopStatus")
    private int stopStatus;  // 0=正常, 1=尚未發車, 2=交管不停靠, 3=末班駛離, 4=今日未營運
}
