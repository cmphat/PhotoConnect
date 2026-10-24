-- ==============================================================================
-- Migration: V016__create_user_profile_and_calendar_events.sql
-- Description: Adds profile fields and social links to users table,
--              and creates calendar_events table.
-- Compatible with: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- ==============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.users', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.users', 'username') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD username NVARCHAR(100) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'avatar_url') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD avatar_url NVARCHAR(500) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'avatar_public_id') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD avatar_public_id NVARCHAR(255) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'bio') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD bio NVARCHAR(1000) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'location') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD location NVARCHAR(150) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'website_url') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD website_url NVARCHAR(255) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'facebook_url') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD facebook_url NVARCHAR(255) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'instagram_url') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD instagram_url NVARCHAR(255) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'tiktok_url') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD tiktok_url NVARCHAR(255) NULL;
    END;

    IF COL_LENGTH('dbo.users', 'youtube_url') IS NULL
    BEGIN
        ALTER TABLE dbo.users ADD youtube_url NVARCHAR(255) NULL;
    END;
END;

IF OBJECT_ID('dbo.calendar_events', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.calendar_events (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        title NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000) NULL,
        start_at DATETIME2 NOT NULL,
        end_at DATETIME2 NOT NULL,
        event_type NVARCHAR(50) NOT NULL CONSTRAINT DF_calendar_events_type DEFAULT 'PERSONAL',
        visibility NVARCHAR(50) NOT NULL CONSTRAINT DF_calendar_events_vis DEFAULT 'PRIVATE',
        created_at DATETIME2 NOT NULL CONSTRAINT DF_calendar_events_created DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT FK_calendar_events_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_calendar_events_user_id ON dbo.calendar_events(user_id);
    CREATE INDEX idx_calendar_events_start_at ON dbo.calendar_events(start_at);
    CREATE INDEX idx_calendar_events_end_at ON dbo.calendar_events(end_at);
END;

COMMIT TRANSACTION;
