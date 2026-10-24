-- =============================================================================
-- Migration: V018__create_booking_history.sql
-- Description: Creates booking_history table for state machine transition audit trail.
-- Compatibility: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- =============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.booking_history', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.booking_history (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        old_status VARCHAR(50) NOT NULL,
        new_status VARCHAR(50) NOT NULL,
        changed_by_user_id BIGINT NOT NULL,
        changed_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        reason NVARCHAR(500) NULL,
        CONSTRAINT fk_booking_history_booking FOREIGN KEY (booking_id)
            REFERENCES dbo.bookings(id) ON DELETE CASCADE,
        CONSTRAINT fk_booking_history_user FOREIGN KEY (changed_by_user_id)
            REFERENCES dbo.users(id)
    );

    CREATE INDEX idx_booking_history_booking ON dbo.booking_history(booking_id);
    CREATE INDEX idx_booking_history_changed_at ON dbo.booking_history(changed_at);
END;

COMMIT TRANSACTION;
