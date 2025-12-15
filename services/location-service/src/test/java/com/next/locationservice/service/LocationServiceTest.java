package com.next.locationservice.service;

import com.next.common.domain.model.Location;
import com.next.locationservice.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private LocationService locationService;

    private Location location;

    @BeforeEach
    void setUp() {
        location = Location.of("vehicle-1", 37.5665, 126.9780);
        location.setId("loc-1");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveLocation_ShouldSaveAndCache() {

        when(locationRepository.save(any(Location.class))).thenReturn(location);

        Location result = locationService.saveLocation(location);

        assertNotNull(result);
        verify(locationRepository).save(location);
        verify(valueOperations).set(anyString(), eq(location), any());
    }

    @Test
    void getLatestLocation_FromCache_ShouldReturnCachedLocation() {

        when(valueOperations.get("location:vehicle-1")).thenReturn(location);

        Location result = locationService.getLatestLocation("vehicle-1");

        assertNotNull(result);
        assertEquals("vehicle-1", result.getVehicleId());
        verify(valueOperations).get("location:vehicle-1");
        verify(locationRepository, never()).findTopByVehicleIdOrderByTimestampDesc(anyString());
    }

    @Test
    void getLatestLocation_CacheMiss_ShouldQueryDatabase() {

        when(valueOperations.get("location:vehicle-1")).thenReturn(null);
        when(locationRepository.findTopByVehicleIdOrderByTimestampDesc("vehicle-1")).thenReturn(location);

        Location result = locationService.getLatestLocation("vehicle-1");

        assertNotNull(result);
        verify(valueOperations).get("location:vehicle-1");
        verify(locationRepository).findTopByVehicleIdOrderByTimestampDesc("vehicle-1");
    }
}
