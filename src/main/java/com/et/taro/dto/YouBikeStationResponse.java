package com.et.taro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouBikeStationResponse {
    private String stationNo;
    private String name;
    private String district;
    private String address;
    private int totalSlots;
    private int availableBikes;
    private int emptySlots;
    private double lat;
    private double lng;
    private int distanceMeters;
    private String updatedAt;
}
