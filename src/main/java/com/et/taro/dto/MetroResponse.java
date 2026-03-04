package com.et.taro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MetroResponse {

    private String station;
    private double distanceMeters;
    private NearestExit nearestExit;
    private List<DirectionArrival> directions;

    @Data
    @Builder
    public static class NearestExit {
        private String exitName;
        private String locationDescription;
        private double distanceMeters;
        private boolean elevator;
    }

    @Data
    @Builder
    public static class DirectionArrival {
        private String lineId;
        private String lineName;
        private String color;
        private int direction;
        private List<TrainInfo> trains;
    }

    @Data
    @Builder
    public static class TrainInfo {
        private String destination;
        private String arrivalTime;
        private int waitMin;
    }
}
