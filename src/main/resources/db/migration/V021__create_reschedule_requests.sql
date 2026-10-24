-- =============================================================================
-- Migration: V021__create_reschedule_requests.sql
-- Description: Creates reschedule_requests table for booking date/time rescheduling
--              and adds cancellation metadata to bookings.
-- Compatibility: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- =============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.reschedule_requests', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.reschedule_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        requested_by_user_id BIGINT NOT NULL,
        proposed_date DATE NOT NULL,
        proposed_time TIME NOT NULL,
        reason NVARCHAR(500) NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_reschedule_status DEFAULT 'PENDING',
        response_note NVARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_reschedule_created DEFAULT SYSUTCDATETIME(),
        resolved_at DATETIME2 NULL,
        CONSTRAINT fk_reschedule_booking FOREIGN KEY (booking_id)
            REFERENCES dbo.bookings(id) ON DELETE CASCADE,
        CONSTRAINT fk_reschedule_user FOREIGN KEY (requested_by_user_id)
            REFERENCES dbo.users(id)
    );

    CREATE INDEX idx_reschedule_booking ON dbo.reschedule_requests(booking_id);
    CREATE INDEX idx_reschedule_status ON dbo.reschedule_requests(booking_id, status);
    CREATE INDEX idx_reschedule_created ON dbo.reschedule_requests(created_at);
END;

IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.bookings', 'cancellation_reason') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD cancellation_reason NVARCHAR(500) NULL;
    END;

    IF COL_LENGTH('dbo.bookings', 'cancelled_by_user_id') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD cancelled_by_user_id BIGINT NULL;
        ALTER TABLE dbo.bookings ADD CONSTRAINT fk_bookings_cancelled_by FOREIGN KEY (cancelled_by_user_id) REFERENCES dbo.users(id);
    END;

    IF COL_LENGTH('dbo.bookings', 'cancelled_at') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD cancelled_at DATETIME2 NULL;
    END;
END;

COMMIT TRANSACTION;
