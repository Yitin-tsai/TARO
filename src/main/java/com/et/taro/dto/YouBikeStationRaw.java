package com.et.taro.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouBikeStationRaw {
    private String sno;                      // 站點代號
    private String sna;                      // 站名，含 "YouBike2.0_" 前綴
    private String sarea;                    // 行政區
    private String ar;                       // 地址
    private String act;                      // "1" = 啟用中
    private String mday;                     // 更新時間，格式 "2026-03-04 10:41:18"

    @JsonProperty("Quantity")
    @JsonAlias("tot_quantity")
    private int quantity;                    // 總車位

    @JsonProperty("available_rent_bikes")
    @JsonAlias("sbi_quantity")
    private int availableRentBikes;          // 可借車輛數

    @JsonProperty("available_return_bikes")
    @JsonAlias("bemp")
    private int availableReturnBikes;        // 可還空位數

    @JsonAlias("lat")
    private double latitude;

    @JsonAlias("lng")
    private double longitude;
}
