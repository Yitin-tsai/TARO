package com.et.taro.client;

import com.et.taro.dto.tdx.TdxMetroStation;
import com.et.taro.dto.tdx.TdxStationExit;
import com.et.taro.dto.tdx.TdxStationTimeTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetroClient {

    private final WebClient tdxWebClient;
    private final TdxAuthClient tdxAuthClient;

    @Cacheable(value = "metroStations", sync = true)
    public List<TdxMetroStation> fetchAllStations() {
        log.info("Fetching all metro stations from TDX");

        List<TdxMetroStation> stations = tdxWebClient.get()
                .uri("/v2/Rail/Metro/Station/TRTC?$format=JSON")
                .headers(h -> h.setBearerAuth(tdxAuthClient.getToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TdxMetroStation>>() {})
                .block();

        log.info("Fetched {} metro stations", stations != null ? stations.size() : 0);
        return stations;
    }

    @Cacheable(value = "metroTimeTables", sync = true)
    public List<TdxStationTimeTable> fetchAllTimeTables() {
        log.info("Fetching all metro station timetables from TDX");

        List<TdxStationTimeTable> timeTables = tdxWebClient.get()
                .uri("/v2/Rail/Metro/StationTimeTable/TRTC?$format=JSON")
                .headers(h -> h.setBearerAuth(tdxAuthClient.getToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TdxStationTimeTable>>() {})
                .block();

        log.info("Fetched {} timetable entries", timeTables != null ? timeTables.size() : 0);
        return timeTables;
    }

    @Cacheable(value = "metroExits", sync = true)
    public List<TdxStationExit> fetchAllExits() {
        log.info("Fetching all metro station exits from TDX");

        List<TdxStationExit> exits = tdxWebClient.get()
                .uri("/v2/Rail/Metro/StationExit/TRTC?$format=JSON")
                .headers(h -> h.setBearerAuth(tdxAuthClient.getToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TdxStationExit>>() {})
                .block();

        log.info("Fetched {} exit entries", exits != null ? exits.size() : 0);
        return exits;
    }
}
