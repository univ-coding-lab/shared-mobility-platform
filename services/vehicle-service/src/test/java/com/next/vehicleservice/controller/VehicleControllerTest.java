package com.next.vehicleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.enums.VehicleType;
import com.next.common.domain.model.Vehicle;
import com.next.vehicleservice.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private List<Vehicle> availableVehicles;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .serialNumber("SN-001")
                .type(VehicleType.E_SCOOTER)
                .model("Model X")
                .manufacturer("TechCo")
                .status(VehicleStatus.AVAILABLE)
                .batteryLevel(85)
                .iotDeviceId("device-123")
                .build();
        vehicle.setId("vehicle-1");

        Vehicle vehicle2 = Vehicle.builder()
                .serialNumber("SN-002")
                .type(VehicleType.BICYCLE)
                .model("Model Y")
                .manufacturer("BikeInc")
                .status(VehicleStatus.AVAILABLE)
                .batteryLevel(90)
                .build();
        vehicle2.setId("vehicle-2");

        availableVehicles = Arrays.asList(vehicle, vehicle2);
    }

    @Test
    void getAvailableVehicles_ShouldReturn200WithVehicleList() throws Exception {

        when(vehicleService.getAvailableVehicles()).thenReturn(availableVehicles);

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("vehicle-1"))
                .andExpect(jsonPath("$.data[0].type").value("E_SCOOTER"))
                .andExpect(jsonPath("$.data[1].id").value("vehicle-2"));
    }

    @Test
    void getVehicle_WithValidId_ShouldReturn200() throws Exception {

        when(vehicleService.getVehicle("vehicle-1")).thenReturn(vehicle);

        mockMvc.perform(get("/vehicles/vehicle-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("vehicle-1"))
                .andExpect(jsonPath("$.data.type").value("E_SCOOTER"))
                .andExpect(jsonPath("$.data.model").value("Model X"))
                .andExpect(jsonPath("$.data.batteryLevel").value(85));
    }

    @Test
    void createVehicle_WithValidData_ShouldReturn200() throws Exception {

        Vehicle newVehicle = Vehicle.builder()
                .serialNumber("SN-NEW")
                .type(VehicleType.E_SCOOTER)
                .model("New Model")
                .manufacturer("NewCo")
                .batteryLevel(100)
                .build();

        when(vehicleService.createVehicle(any(Vehicle.class))).thenReturn(vehicle);

        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("vehicle-1"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }

    @Test
    void lockVehicle_WithValidId_ShouldReturn200() throws Exception {

        doNothing().when(vehicleService).lockVehicle(anyString());

        mockMvc.perform(post("/vehicles/vehicle-1/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle locked"));
    }

    @Test
    void unlockVehicle_WithValidId_ShouldReturn200() throws Exception {

        doNothing().when(vehicleService).unlockVehicle(anyString());

        mockMvc.perform(post("/vehicles/vehicle-1/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle unlocked"));
    }
}
