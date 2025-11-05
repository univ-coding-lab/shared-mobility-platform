-- Shared Mobility Platform - PostgreSQL Initialization Script
-- This script creates databases for each microservice

-- Create databases for each service
CREATE DATABASE user_service_db;
CREATE DATABASE vehicle_service_db;
CREATE DATABASE rental_service_db;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON DATABASE user_service_db TO smpuser;
GRANT ALL PRIVILEGES ON DATABASE vehicle_service_db TO smpuser;
GRANT ALL PRIVILEGES ON DATABASE rental_service_db TO smpuser;

-- Connect to user_service_db and create schema
\c user_service_db;

CREATE SCHEMA IF NOT EXISTS users;

-- Connect to vehicle_service_db and create schema
\c vehicle_service_db;

CREATE SCHEMA IF NOT EXISTS vehicles;

-- Connect to rental_service_db and create schema
\c rental_service_db;

CREATE SCHEMA IF NOT EXISTS rentals;

-- Log success message
SELECT 'PostgreSQL databases initialized successfully' AS status;
