package com.et.taro.controller;

import com.et.taro.dto.DashboardResponse;
import com.et.taro.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam double lat,
            @RequestParam double lng) {

        DashboardResponse response = dashboardService.getDashboard(lat, lng);
        return ResponseEntity.ok(response);
    }
}
