package com.et.taro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusResponse {

    private List<NearbyStop> stops;

    @Data
    @Builder
    public static class NearbyStop {
        private String stationName;
        private double distanceMeters;
        private List<RouteArrival> routes;
    }

    @Data
    @Builder
    public static class RouteArrival {
        private String routeName;
        private int direction;
        private String destination;
        private Integer estimateMin;  // null = 無資料
        private String status;
    }
}
