package com.et.taro.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetroDataInitializer {

    private final MetroClient metroClient;

    @EventListener(ApplicationReadyEvent.class)
    public void preloadMetroData() {
        log.info("Preloading metro static data...");
        metroClient.fetchAllStations();
        metroClient.fetchAllTimeTables();
        metroClient.fetchAllExits();
        log.info("Metro static data preloaded");
    }
}
