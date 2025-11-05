package com.next.locationservice.service;

import com.next.common.domain.model.Location;
import com.next.locationservice.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Location saveLocation(Location location) {
        Location saved = locationRepository.save(location);
        String cacheKey = "location:" + location.getVehicleId();
        redisTemplate.opsForValue().set(cacheKey, saved, Duration.ofSeconds(30));
        log.info("Location saved for vehicle: {}", location.getVehicleId());
        return saved;
    }

    public Location getLatestLocation(String vehicleId) {
        String cacheKey = "location:" + vehicleId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (Location) cached;
        }
        return locationRepository.findTopByVehicleIdOrderByTimestampDesc(vehicleId);
    }
}
