/*
  ==============================================================================
  Migration: V012__add_professional_profile_fields.sql
  Task: TASK-C01 Professional Photographer Profile Editing
  Description: Adds professional profile fields to photographer_profiles:
               headline, country, specialties, website_url, instagram_url,
               facebook_url, equipment_summary, languages, travel_available.
  Compatible with: Microsoft SQL Server 2019+
  Forward-only, non-destructive, idempotent.
  ==============================================================================
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Add headline column (NVARCHAR(255) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'headline') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD headline NVARCHAR(255) NULL;
END

-- 2. Add country column (NVARCHAR(100) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'country') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD country NVARCHAR(100) NULL;
END

-- 3. Add specialties column (NVARCHAR(255) NULL)
-- Stores comma-delimited controlled taxonomy tokens (e.g. 'PORTRAIT,WEDDING')
IF COL_LENGTH('dbo.photographer_profiles', 'specialties') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD specialties NVARCHAR(255) NULL;
END

-- 4. Add website_url column (NVARCHAR(255) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'website_url') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD website_url NVARCHAR(255) NULL;
END

-- 5. Add instagram_url column (NVARCHAR(255) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'instagram_url') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD instagram_url NVARCHAR(255) NULL;
END

-- 6. Add facebook_url column (NVARCHAR(255) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'facebook_url') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD facebook_url NVARCHAR(255) NULL;
END

-- 7. Add equipment_summary column (NVARCHAR(500) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'equipment_summary') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD equipment_summary NVARCHAR(500) NULL;
END

-- 8. Add languages column (NVARCHAR(150) NULL)
IF COL_LENGTH('dbo.photographer_profiles', 'languages') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD languages NVARCHAR(150) NULL;
END

-- 9. Add travel_available column (BIT NOT NULL DEFAULT 0)
IF COL_LENGTH('dbo.photographer_profiles', 'travel_available') IS NULL
BEGIN
    ALTER TABLE dbo.photographer_profiles
    ADD travel_available BIT NOT NULL DEFAULT 0;
END

COMMIT TRANSACTION;
