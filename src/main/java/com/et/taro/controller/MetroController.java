package com.et.taro.controller;

import com.et.taro.dto.MetroResponse;
import com.et.taro.service.MetroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetroController {

    private final MetroService metroService;

    @GetMapping("/metro/arrivals")
    public ResponseEntity<MetroResponse> getArrivals(
            @RequestParam double lat,
            @RequestParam double lng) {

        MetroResponse response = metroService.getNearbyArrivals(lat, lng);
        return ResponseEntity.ok(response);
    }
}
