package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TdxServiceDay {

    @JsonProperty("ServiceTag")
    private String serviceTag;

    @JsonProperty("Monday")
    private boolean monday;

    @JsonProperty("Tuesday")
    private boolean tuesday;

    @JsonProperty("Wednesday")
    private boolean wednesday;

    @JsonProperty("Thursday")
    private boolean thursday;

    @JsonProperty("Friday")
    private boolean friday;

    @JsonProperty("Saturday")
    private boolean saturday;

    @JsonProperty("Sunday")
    private boolean sunday;

    @JsonProperty("NationalHolidays")
    private boolean nationalHolidays;
}
