package com.next.locationservice.repository;

import com.next.common.domain.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
    List<Location> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    Location findTopByVehicleIdOrderByTimestampDesc(String vehicleId);
}
