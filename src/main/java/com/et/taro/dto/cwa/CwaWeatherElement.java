package com.et.taro.dto.cwa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CwaWeatherElement {

    @JsonProperty("ElementName")
    private String elementName;

    @JsonProperty("Time")
    private List<CwaTimeEntry> time;
}
