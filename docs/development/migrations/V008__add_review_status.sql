-- ==============================================================================
-- Migration: V008__add_review_status.sql
-- Task: TASK-021 Admin Review Moderation
-- Description: Adds status column to reviews table with default 'VISIBLE'.
-- Compatible with: Microsoft SQL Server 2019+
-- ==============================================================================

USE PhotoConnect;
GO

IF NOT EXISTS (
    SELECT 1 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.reviews') 
      AND name = 'status'
)
BEGIN
    ALTER TABLE dbo.reviews 
    ADD status VARCHAR(20) NOT NULL 
    CONSTRAINT DF_reviews_status DEFAULT 'VISIBLE';

    PRINT 'Added column status to reviews table with default VISIBLE.';
END
ELSE
BEGIN
    PRINT 'Column status already exists on reviews table.';
END
GO
