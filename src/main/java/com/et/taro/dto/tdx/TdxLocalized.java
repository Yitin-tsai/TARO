package com.et.taro.dto.tdx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TdxLocalized {

    @JsonProperty("Zh_tw")
    private String zhTw;

    @JsonProperty("En")
    private String en;
}
