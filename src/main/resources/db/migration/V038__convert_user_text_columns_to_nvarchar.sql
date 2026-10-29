-- ==============================================================================
-- Migration: V038__convert_user_text_columns_to_nvarchar.sql
-- Description: Convert user-entered text columns from VARCHAR to NVARCHAR across
--              messages, reviews, notifications, bookings, service_packages,
--              users, photographer_profiles, booking_history, delivery_albums,
--              reschedule_requests, and photographer_unavailable_dates.
-- Fixes Vietnamese diacritic and full Unicode character corruption (e.g. ? marks).
-- Compatible with: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- ==============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. messages
IF COL_LENGTH('dbo.messages', 'content') IS NOT NULL
BEGIN
    ALTER TABLE dbo.messages ALTER COLUMN content NVARCHAR(2000) NOT NULL;
    PRINT 'Converted dbo.messages(content) to NVARCHAR(2000).';
END;

-- 2. reviews
IF COL_LENGTH('dbo.reviews', 'comment') IS NOT NULL
BEGIN
    ALTER TABLE dbo.reviews ALTER COLUMN comment NVARCHAR(1000) NULL;
    PRINT 'Converted dbo.reviews(comment) to NVARCHAR(1000).';
END;

-- 3. notifications
IF COL_LENGTH('dbo.notifications', 'title') IS NOT NULL
    ALTER TABLE dbo.notifications ALTER COLUMN title NVARCHAR(255) NOT NULL;
IF COL_LENGTH('dbo.notifications', 'message') IS NOT NULL
    ALTER TABLE dbo.notifications ALTER COLUMN message NVARCHAR(1000) NOT NULL;

-- 4. bookings
IF COL_LENGTH('dbo.bookings', 'notes') IS NOT NULL
    ALTER TABLE dbo.bookings ALTER COLUMN notes NVARCHAR(1000) NULL;
IF COL_LENGTH('dbo.bookings', 'location') IS NOT NULL
    ALTER TABLE dbo.bookings ALTER COLUMN location NVARCHAR(255) NULL;
IF COL_LENGTH('dbo.bookings', 'package_name') IS NOT NULL
    ALTER TABLE dbo.bookings ALTER COLUMN package_name NVARCHAR(150) NULL;

-- 5. service_packages
IF COL_LENGTH('dbo.service_packages', 'name') IS NOT NULL
    ALTER TABLE dbo.service_packages ALTER COLUMN name NVARCHAR(150) NOT NULL;
IF COL_LENGTH('dbo.service_packages', 'description') IS NOT NULL
    ALTER TABLE dbo.service_packages ALTER COLUMN description NVARCHAR(1000) NULL;

-- 6. users
IF COL_LENGTH('dbo.users', 'full_name') IS NOT NULL
    ALTER TABLE dbo.users ALTER COLUMN full_name NVARCHAR(150) NOT NULL;
IF COL_LENGTH('dbo.users', 'location') IS NOT NULL
    ALTER TABLE dbo.users ALTER COLUMN location NVARCHAR(150) NULL;

-- 7. photographer_profiles
IF COL_LENGTH('dbo.photographer_profiles', 'display_name') IS NOT NULL
    ALTER TABLE dbo.photographer_profiles ALTER COLUMN display_name NVARCHAR(150) NOT NULL;

-- 8. booking_history
IF COL_LENGTH('dbo.booking_history', 'reason') IS NOT NULL
    ALTER TABLE dbo.booking_history ALTER COLUMN reason NVARCHAR(500) NULL;

-- 9. delivery_albums
IF COL_LENGTH('dbo.delivery_albums', 'notes') IS NOT NULL
    ALTER TABLE dbo.delivery_albums ALTER COLUMN notes NVARCHAR(1000) NULL;

-- 10. reschedule_requests
IF COL_LENGTH('dbo.reschedule_requests', 'reason') IS NOT NULL
    ALTER TABLE dbo.reschedule_requests ALTER COLUMN reason NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.reschedule_requests', 'response_note') IS NOT NULL
    ALTER TABLE dbo.reschedule_requests ALTER COLUMN response_note NVARCHAR(500) NULL;

-- 11. photographer_unavailable_dates
IF COL_LENGTH('dbo.photographer_unavailable_dates', 'reason') IS NOT NULL
    ALTER TABLE dbo.photographer_unavailable_dates ALTER COLUMN reason NVARCHAR(255) NULL;

COMMIT TRANSACTION;
