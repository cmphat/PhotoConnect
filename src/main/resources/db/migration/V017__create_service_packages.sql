-- =============================================================================
-- Migration: V017__create_service_packages.sql
-- Description: Creates service_packages table and adds package snapshot columns to bookings.
-- Compatibility: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- =============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Create service_packages table
IF OBJECT_ID('dbo.service_packages', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.service_packages (
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
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_service_packages_photographer FOREIGN KEY (photographer_profile_id)
            REFERENCES dbo.photographer_profiles(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_service_packages_photographer ON dbo.service_packages(photographer_profile_id);
    CREATE INDEX idx_service_packages_category ON dbo.service_packages(category);
    CREATE INDEX idx_service_packages_active ON dbo.service_packages(active);
END;

-- 2. Add package snapshot fields to bookings
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.bookings', 'service_package_id') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD service_package_id BIGINT NULL;
        ALTER TABLE dbo.bookings ADD CONSTRAINT fk_bookings_service_package FOREIGN KEY (service_package_id)
            REFERENCES dbo.service_packages(id) ON DELETE SET NULL;
    END;

    IF COL_LENGTH('dbo.bookings', 'package_name') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD package_name NVARCHAR(150) NULL;
    END;

    IF COL_LENGTH('dbo.bookings', 'duration_minutes') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD duration_minutes INT NOT NULL CONSTRAINT df_bookings_duration DEFAULT 120;
    END;

    IF COL_LENGTH('dbo.bookings', 'edited_photo_count') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD edited_photo_count INT NULL;
    END;

    IF COL_LENGTH('dbo.bookings', 'delivery_days') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD delivery_days INT NULL;
    END;
END;

COMMIT TRANSACTION;
