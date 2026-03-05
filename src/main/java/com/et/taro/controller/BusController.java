package com.et.taro.controller;

import com.et.taro.dto.BusResponse;
import com.et.taro.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @GetMapping("/bus/arrivals")
    public ResponseEntity<BusResponse> getArrivals(
            @RequestParam double lat,
            @RequestParam double lng) {

        BusResponse response = busService.getNearbyArrivals(lat, lng);
        return ResponseEntity.ok(response);
    }
}
