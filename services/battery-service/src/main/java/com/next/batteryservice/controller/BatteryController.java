package com.next.batteryservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.BatteryLog;
import com.next.batteryservice.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battery")
@RequiredArgsConstructor
public class BatteryController {
    private final BatteryService batteryService;

    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<BatteryLog>> saveBatteryLog(@RequestBody BatteryLog batteryLog) {
        return ResponseEntity.ok(ApiResponse.success(batteryService.saveBatteryLog(batteryLog)));
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<ApiResponse<BatteryLog>> getLatestBatteryLog(@PathVariable String vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(batteryService.getLatestBatteryLog(vehicleId)));
    }
}
