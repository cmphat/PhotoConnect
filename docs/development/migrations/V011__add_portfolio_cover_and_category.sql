/*
  ==============================================================================
  Migration: V011__add_portfolio_cover_and_category.sql
  Task: TASK-B02 Portfolio Cover Image, Category & Portfolio Manager Enhancement
  Description: Adds is_cover and category columns to portfolio_images table.
  Compatible with: Microsoft SQL Server 2019+
  Forward-only, non-destructive, idempotent.
  ==============================================================================
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Add is_cover column (BIT NOT NULL DEFAULT 0)
IF COL_LENGTH('dbo.portfolio_images', 'is_cover') IS NULL
BEGIN
    ALTER TABLE dbo.portfolio_images
    ADD is_cover BIT NOT NULL
    CONSTRAINT DF_portfolio_images_is_cover DEFAULT 0;

    PRINT 'Added column is_cover to portfolio_images with default 0.';
END
ELSE
BEGIN
    PRINT 'Column is_cover already exists on portfolio_images.';
END;

-- 2. Add category column (NVARCHAR(50) NULL)
IF COL_LENGTH('dbo.portfolio_images', 'category') IS NULL
BEGIN
    ALTER TABLE dbo.portfolio_images
    ADD category NVARCHAR(50) NULL;

    PRINT 'Added column category to portfolio_images.';
END
ELSE
BEGIN
    PRINT 'Column category already exists on portfolio_images.';
END;

COMMIT TRANSACTION;
