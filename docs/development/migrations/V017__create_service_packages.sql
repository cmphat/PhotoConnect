-- =============================================================================
-- Migration: V017__create_service_packages.sql
-- Description: Creates service_packages table and adds package snapshot columns to bookings.
-- Compatibility: Microsoft SQL Server, PostgreSQL, MySQL
-- =============================================================================

-- 1. Create service_packages table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'service_packages')
BEGIN
    CREATE TABLE service_packages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        photographer_profile_id BIGINT NOT NULL,
        category VARCHAR(50) NOT NULL,
        name NVARCHAR(150) NOT NULL,
        description NVARCHAR(1000) NULL,
        price DECIMAL(18, 2) NOT NULL,
        duration_minutes INT NOT NULL,
        edited_photo_count INT NOT NULL,
        delivery_days INT NOT NULL,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_service_packages_photographer FOREIGN KEY (photographer_profile_id)
            REFERENCES photographer_profiles(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_service_packages_photographer ON service_packages(photographer_profile_id);
    CREATE INDEX idx_service_packages_category ON service_packages(category);
    CREATE INDEX idx_service_packages_active ON service_packages(active);
END;

-- 2. Add package snapshot fields to bookings
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'service_package_id')
BEGIN
    ALTER TABLE bookings ADD service_package_id BIGINT NULL;
    ALTER TABLE bookings ADD CONSTRAINT fk_bookings_service_package FOREIGN KEY (service_package_id)
        REFERENCES service_packages(id) ON DELETE SET NULL;
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'package_name')
BEGIN
    ALTER TABLE bookings ADD package_name NVARCHAR(150) NULL;
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'duration_minutes')
BEGIN
    ALTER TABLE bookings ADD duration_minutes INT NOT NULL CONSTRAINT df_bookings_duration DEFAULT 120;
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'edited_photo_count')
BEGIN
    ALTER TABLE bookings ADD edited_photo_count INT NULL;
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'delivery_days')
BEGIN
    ALTER TABLE bookings ADD delivery_days INT NULL;
END;
