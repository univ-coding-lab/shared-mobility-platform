// Shared Mobility Platform - MongoDB Initialization Script
// This script creates collections and indexes for IoT time-series data

// Switch to iot_logs database
db = db.getSiblingDB('iot_logs');

// Create application user
db.createUser({
  user: 'iotuser',
  pwd: 'iotpass',
  roles: [
    {
      role: 'readWrite',
      db: 'iot_logs'
    }
  ]
});

// Create collections for IoT data
db.createCollection('location_logs', {
  timeseries: {
    timeField: 'timestamp',
    metaField: 'vehicleId',
    granularity: 'seconds'
  }
});

db.createCollection('battery_logs', {
  timeseries: {
    timeField: 'timestamp',
    metaField: 'vehicleId',
    granularity: 'minutes'
  }
});

db.createCollection('vehicle_events', {
  timeseries: {
    timeField: 'timestamp',
    metaField: 'vehicleId',
    granularity: 'seconds'
  }
});

// Create indexes for efficient queries
db.location_logs.createIndex({ vehicleId: 1, timestamp: -1 });
db.location_logs.createIndex({ 'location.coordinates': '2dsphere' });

db.battery_logs.createIndex({ vehicleId: 1, timestamp: -1 });
db.battery_logs.createIndex({ batteryLevel: 1 });

db.vehicle_events.createIndex({ vehicleId: 1, timestamp: -1 });
db.vehicle_events.createIndex({ eventType: 1 });

print('MongoDB collections and indexes initialized successfully');
