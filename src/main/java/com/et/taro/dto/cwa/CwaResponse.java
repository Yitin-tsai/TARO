package com.et.taro.dto.cwa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CwaResponse {

    private String success;
    private Records records;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Records {
        @JsonProperty("Locations")
        private List<Locations> locations;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Locations {
        @JsonProperty("DatasetDescription")
        private String datasetDescription;

        @JsonProperty("LocationsName")
        private String locationsName;

        @JsonProperty("Location")
        private List<CwaLocation> location;
    }
}
