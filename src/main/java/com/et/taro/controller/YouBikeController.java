package com.et.taro.controller;

import com.et.taro.dto.YouBikeStationResponse;
import com.et.taro.service.YouBikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class YouBikeController {

    private final YouBikeService youBikeService;

    @GetMapping("/youbike")
    public ResponseEntity<List<YouBikeStationResponse>> getNearbyStations(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) Integer radius) {

        List<YouBikeStationResponse> stations = youBikeService.findNearby(lat, lng, radius);
        return ResponseEntity.ok(stations);
    }
}
