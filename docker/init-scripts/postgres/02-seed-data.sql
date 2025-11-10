-- Shared Mobility Platform - PostgreSQL Seed Data
-- This script adds sample data for testing and verification

-- Connect to user_service_db
\c user_service_db;

-- Insert sample users (if users table exists)
-- Note: Actual table creation is handled by JPA (ddl-auto: update)
-- This script will add sample data after application first run

-- Connect to vehicle_service_db
\c vehicle_service_db;

-- Sample vehicles will be inserted via application startup or API calls

-- Connect to rental_service_db
\c rental_service_db;

-- Sample rentals will be created via application workflow

-- Log success message
SELECT 'PostgreSQL seed data script loaded successfully' AS status;
SELECT 'Note: Sample data will be populated after application startup' AS note;
