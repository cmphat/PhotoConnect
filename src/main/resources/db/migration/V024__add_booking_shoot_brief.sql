-- =============================================================================
-- Migration: V024__add_booking_shoot_brief.sql
-- Description: Adds shoot brief fields (concept, moodboard, backup location,
--              outfit count, shot list, props, participants) to bookings table.
-- =============================================================================

IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.bookings', 'concept_description') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD concept_description NVARCHAR(1000) NULL;
    END

    IF COL_LENGTH('dbo.bookings', 'moodboard_url') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD moodboard_url VARCHAR(500) NULL;
    END

    IF COL_LENGTH('dbo.bookings', 'backup_location') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD backup_location NVARCHAR(255) NULL;
    END

    IF COL_LENGTH('dbo.bookings', 'outfit_count') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD outfit_count INT NULL;
    END

    IF COL_LENGTH('dbo.bookings', 'shot_list') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD shot_list NVARCHAR(1500) NULL;
    END

    IF COL_LENGTH('dbo.bookings', 'props_needed') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD props_needed NVARCHAR(500) NULL;
    END

    IF COL_LENGTH('dbo.bookings', 'participant_count') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD participant_count INT NULL;
    END
END
