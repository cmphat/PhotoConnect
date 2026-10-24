-- =============================================================================
-- Migration: V020__create_notifications.sql
-- Description: Creates notifications table for persistent user notifications.
-- Compatibility: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- =============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.notifications', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        recipient_id BIGINT NOT NULL,
        type VARCHAR(50) NOT NULL,
        title NVARCHAR(255) NOT NULL,
        message NVARCHAR(1000) NOT NULL,
        reference_id BIGINT NULL,
        target_url VARCHAR(500) NULL,
        read_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_notifications_created DEFAULT SYSUTCDATETIME(),
        CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id)
            REFERENCES dbo.users(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_notifications_recipient ON dbo.notifications(recipient_id);
    CREATE INDEX idx_notifications_unread ON dbo.notifications(recipient_id, read_at);
    CREATE INDEX idx_notifications_created_at ON dbo.notifications(created_at);
END;

COMMIT TRANSACTION;
