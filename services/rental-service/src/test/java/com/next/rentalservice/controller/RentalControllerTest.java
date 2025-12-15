package com.next.rentalservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.model.Rental;
import com.next.rentalservice.dto.RentalEndRequest;
import com.next.rentalservice.dto.RentalStartRequest;
import com.next.rentalservice.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RentalService rentalService;

    @InjectMocks
    private RentalController rentalController;

    private Rental rental;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rentalController).build();
        objectMapper = new ObjectMapper();

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
    void startRental_WithValidParams_ShouldReturn201() throws Exception {
        RentalStartRequest request = RentalStartRequest.builder()
                .userId("user-1")
                .vehicleId("vehicle-1")
                .latitude(37.5665)
                .longitude(126.9780)
                .batteryLevel(85)
                .build();

        when(rentalService.startRental(anyString(), anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(rental);

        mockMvc.perform(post("/rentals/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-1"))
                .andExpect(jsonPath("$.data.vehicleId").value("vehicle-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void endRental_WithValidParams_ShouldReturn200() throws Exception {
        rental.setStatus(RentalStatus.COMPLETED);
        RentalEndRequest request = RentalEndRequest.builder()
                .latitude(37.5700)
                .longitude(126.9800)
                .batteryLevel(60)
                .build();

        when(rentalService.endRental(anyString(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(rental);

        mockMvc.perform(post("/rentals/rental-1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
