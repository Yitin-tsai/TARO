package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TdxTimetableEntry {

    @JsonProperty("Sequence")
    private int sequence;

    @JsonProperty("ArrivalTime")
    private String arrivalTime;

    @JsonProperty("DepartureTime")
    private String departureTime;
}
