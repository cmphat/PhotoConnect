-- =============================================================================
-- Migration: V023__add_photographer_review_audit_and_social_urls.sql
-- Description: Adds rejection_reason, admin_notes, reviewed_at, reviewed_by_admin_id,
--              tiktok_url, and youtube_url to photographer_profiles.
-- =============================================================================

IF OBJECT_ID('dbo.photographer_profiles', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.photographer_profiles', 'tiktok_url') IS NULL
    BEGIN
        ALTER TABLE dbo.photographer_profiles ADD tiktok_url VARCHAR(255) NULL;
    END

    IF COL_LENGTH('dbo.photographer_profiles', 'youtube_url') IS NULL
    BEGIN
        ALTER TABLE dbo.photographer_profiles ADD youtube_url VARCHAR(255) NULL;
    END

    IF COL_LENGTH('dbo.photographer_profiles', 'rejection_reason') IS NULL
    BEGIN
        ALTER TABLE dbo.photographer_profiles ADD rejection_reason NVARCHAR(1000) NULL;
    END

    IF COL_LENGTH('dbo.photographer_profiles', 'admin_notes') IS NULL
    BEGIN
        ALTER TABLE dbo.photographer_profiles ADD admin_notes NVARCHAR(1000) NULL;
    END

    IF COL_LENGTH('dbo.photographer_profiles', 'reviewed_at') IS NULL
    BEGIN
        ALTER TABLE dbo.photographer_profiles ADD reviewed_at DATETIME2 NULL;
    END

    IF COL_LENGTH('dbo.photographer_profiles', 'reviewed_by_admin_id') IS NULL
    BEGIN
        ALTER TABLE dbo.photographer_profiles ADD reviewed_by_admin_id BIGINT NULL;
    END
END
