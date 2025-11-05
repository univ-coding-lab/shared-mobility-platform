package com.next.batteryservice.repository;

import com.next.common.domain.model.BatteryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BatteryLogRepository extends MongoRepository<BatteryLog, String> {
    List<BatteryLog> findByVehicleIdOrderByTimestampDesc(String vehicleId);
    BatteryLog findTopByVehicleIdOrderByTimestampDesc(String vehicleId);
}
