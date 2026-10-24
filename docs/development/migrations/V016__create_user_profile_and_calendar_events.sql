-- V016__create_user_profile_and_calendar_events.sql
-- Add profile fields and social links to users table, and create calendar_events table

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'username')
BEGIN
    ALTER TABLE users ADD username NVARCHAR(100) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'avatar_url')
BEGIN
    ALTER TABLE users ADD avatar_url NVARCHAR(500) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'avatar_public_id')
BEGIN
    ALTER TABLE users ADD avatar_public_id NVARCHAR(255) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'bio')
BEGIN
    ALTER TABLE users ADD bio NVARCHAR(1000) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'location')
BEGIN
    ALTER TABLE users ADD location NVARCHAR(150) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'website_url')
BEGIN
    ALTER TABLE users ADD website_url NVARCHAR(255) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'facebook_url')
BEGIN
    ALTER TABLE users ADD facebook_url NVARCHAR(255) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'instagram_url')
BEGIN
    ALTER TABLE users ADD instagram_url NVARCHAR(255) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'tiktok_url')
BEGIN
    ALTER TABLE users ADD tiktok_url NVARCHAR(255) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'youtube_url')
BEGIN
    ALTER TABLE users ADD youtube_url NVARCHAR(255) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'calendar_events')
BEGIN
    CREATE TABLE calendar_events (
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
        CONSTRAINT FK_calendar_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_calendar_events_user_id ON calendar_events(user_id);
    CREATE INDEX idx_calendar_events_start_at ON calendar_events(start_at);
    CREATE INDEX idx_calendar_events_end_at ON calendar_events(end_at);
END;
