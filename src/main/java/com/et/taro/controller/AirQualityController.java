package com.et.taro.controller;

import com.et.taro.dto.AirQualityResponse;
import com.et.taro.service.AirQualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AirQualityController {

    private final AirQualityService airQualityService;

    @GetMapping("/air-quality")
    public ResponseEntity<AirQualityResponse> getAirQuality(
            @RequestParam double lat,
            @RequestParam double lng) {

        AirQualityResponse aqi = airQualityService.getAirQuality(lat, lng);
        return ResponseEntity.ok(aqi);
    }
}
