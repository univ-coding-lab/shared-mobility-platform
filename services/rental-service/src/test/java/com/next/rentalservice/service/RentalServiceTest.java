package com.next.rentalservice.service;

import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.model.Rental;
import com.next.common.event.publisher.EventPublisher;
import com.next.rentalservice.repository.RentalRepository;
import com.next.rentalservice.saga.RentalSaga;
import com.next.rentalservice.saga.RentalSagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private RentalSagaOrchestrator sagaOrchestrator;

    @InjectMocks
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
    void startRental_ShouldCreateRentalAndPublishEvent() {
        RentalSaga mockSaga = mock(RentalSaga.class);
        when(mockSaga.hasFailed()).thenReturn(false);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(sagaOrchestrator.executeSaga(anyString(), anyString(), anyString())).thenReturn(mockSaga);
        when(eventPublisher.publishAsync(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        Rental result = rentalService.startRental("user-1", "vehicle-1", 37.5665, 126.9780, 85);

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        verify(rentalRepository).save(any(Rental.class));
        verify(sagaOrchestrator).executeSaga(anyString(), anyString(), anyString());
        verify(eventPublisher).publishAsync(anyString(), anyString(), any());
    }

    @Test
    void endRental_WithValidId_ShouldCompleteRentalAndPublishEvent() {
        when(rentalRepository.findById("rental-1")).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(eventPublisher.publishAsync(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        Rental result = rentalService.endRental("rental-1", 37.5700, 126.9800, 60);

        assertNotNull(result);
        verify(rentalRepository).findById("rental-1");
        verify(rentalRepository).save(rental);
        verify(eventPublisher).publishAsync(anyString(), anyString(), any());
    }

    @Test
    void endRental_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(rentalRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            rentalService.endRental("invalid-id", 37.5700, 126.9800, 60));
        verify(rentalRepository).findById("invalid-id");
        verify(eventPublisher, never()).publishAsync(anyString(), anyString(), any());
    }

    @Test
    void getRental_WithValidId_ShouldReturnRental() {

        when(rentalRepository.findById("rental-1")).thenReturn(Optional.of(rental));

        Rental result = rentalService.getRental("rental-1");

        assertNotNull(result);
        assertEquals("rental-1", result.getId());
        verify(rentalRepository).findById("rental-1");
    }

    @Test
    void getRental_WithInvalidId_ShouldThrowResourceNotFoundException() {

        when(rentalRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rentalService.getRental("invalid-id"));
        verify(rentalRepository).findById("invalid-id");
    }
}
