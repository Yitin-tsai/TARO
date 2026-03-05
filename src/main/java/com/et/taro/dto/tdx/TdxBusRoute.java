package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TdxBusRoute {

    @JsonProperty("RouteID")
    private String routeId;

    @JsonProperty("RouteName")
    private TdxLocalized routeName;

    @JsonProperty("DepartureStopNameZh")
    private String departureStopNameZh;

    @JsonProperty("DestinationStopNameZh")
    private String destinationStopNameZh;
}
