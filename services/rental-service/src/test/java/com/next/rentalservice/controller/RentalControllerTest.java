package com.next.rentalservice.controller;

import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.model.Rental;
import com.next.rentalservice.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalService rentalService;

    private Rental rental;

    @BeforeEach
    void setUp() {
        rental = Rental.builder()
                .userId("user-1")
                .vehicleId("vehicle-1")
                .status(RentalStatus.ACTIVE)
                .startLatitude(37.5665)
                .startLongitude(126.9780)
                .startBatteryLevel(85)
                .build();
        rental.setId("rental-1");
    }

    @Test
    void startRental_WithValidParams_ShouldReturn200() throws Exception {

        when(rentalService.startRental(anyString(), anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(rental);

        mockMvc.perform(post("/rentals/start")
                        .param("userId", "user-1")
                        .param("vehicleId", "vehicle-1")
                        .param("lat", "37.5665")
                        .param("lon", "126.9780")
                        .param("batteryLevel", "85"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-1"))
                .andExpect(jsonPath("$.data.vehicleId").value("vehicle-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void endRental_WithValidParams_ShouldReturn200() throws Exception {

        rental.setStatus(RentalStatus.COMPLETED);
        when(rentalService.endRental(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(rental);

        mockMvc.perform(post("/rentals/rental-1/end")
                        .param("lat", "37.5700")
                        .param("lon", "126.9800")
                        .param("batteryLevel", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rental-1"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void getRental_WithValidId_ShouldReturn200() throws Exception {

        when(rentalService.getRental("rental-1")).thenReturn(rental);

        mockMvc.perform(get("/rentals/rental-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rental-1"))
                .andExpect(jsonPath("$.data.userId").value("user-1"))
                .andExpect(jsonPath("$.data.vehicleId").value("vehicle-1"));
    }
}
