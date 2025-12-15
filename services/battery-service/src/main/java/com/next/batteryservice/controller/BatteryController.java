package com.next.batteryservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.BatteryLog;
import com.next.batteryservice.dto.BatteryLogRequest;
import com.next.batteryservice.dto.BatteryLogResponse;
import com.next.batteryservice.service.BatteryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battery")
@RequiredArgsConstructor
public class BatteryController {
    private final BatteryService batteryService;

    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<BatteryLogResponse>> saveBatteryLog(@Valid @RequestBody BatteryLogRequest request) {
        BatteryLog batteryLog = batteryService.saveBatteryLog(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Battery log saved", BatteryLogResponse.from(batteryLog)));
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<ApiResponse<BatteryLogResponse>> getLatestBatteryLog(@PathVariable String vehicleId) {
        BatteryLog batteryLog = batteryService.getLatestBatteryLog(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(BatteryLogResponse.from(batteryLog)));
    }

    private BatteryLog toEntity(BatteryLogRequest request) {
        return BatteryLog.builder()
                .vehicleId(request.getVehicleId())
                .batteryLevel(request.getBatteryLevel())
                .voltage(request.getVoltage())
                .current(request.getCurrent())
                .temperature(request.getTemperature())
                .cycleCount(request.getCycleCount())
                .healthStatus(request.getHealthStatus())
                .isCharging(request.getIsCharging())
                .estimatedRangeKm(request.getEstimatedRangeKm())
                .estimatedTimeToFullCharge(request.getEstimatedTimeToFullCharge())
                .chargingStationId(request.getChargingStationId())
                .source(request.getSource())
                .build();
    }
}
