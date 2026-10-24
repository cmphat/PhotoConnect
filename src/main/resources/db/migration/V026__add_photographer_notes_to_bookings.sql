-- Flyway Migration V026: Add private photographer CRM notes to bookings
-- Author: PhotoConnect Engineering
-- Description: Adds photographer_notes column for creator internal CRM tracking.

IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.bookings', 'photographer_notes') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD photographer_notes NVARCHAR(1500) NULL;
    END
END
