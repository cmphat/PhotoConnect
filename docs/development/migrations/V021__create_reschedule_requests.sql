-- =============================================================================
-- Migration: V021__create_reschedule_requests.sql
-- Description: Creates reschedule_requests table for booking date/time rescheduling
--              and adds cancellation metadata to bookings.
-- Compatibility: Microsoft SQL Server, PostgreSQL, MySQL
-- =============================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'reschedule_requests')
BEGIN
    CREATE TABLE reschedule_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        requested_by_user_id BIGINT NOT NULL,
        proposed_date DATE NOT NULL,
        proposed_time TIME NOT NULL,
        reason NVARCHAR(500) NULL,
        status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
        response_note NVARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        resolved_at DATETIME2 NULL,
        CONSTRAINT fk_reschedule_booking FOREIGN KEY (booking_id)
            REFERENCES bookings(id) ON DELETE CASCADE,
        CONSTRAINT fk_reschedule_user FOREIGN KEY (requested_by_user_id)
            REFERENCES users(id)
    );

    CREATE INDEX idx_reschedule_booking ON reschedule_requests(booking_id);
    CREATE INDEX idx_reschedule_status ON reschedule_requests(booking_id, status);
    CREATE INDEX idx_reschedule_created ON reschedule_requests(created_at);
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'cancellation_reason')
BEGIN
    ALTER TABLE bookings ADD cancellation_reason NVARCHAR(500) NULL;
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'cancelled_by_user_id')
BEGIN
    ALTER TABLE bookings ADD cancelled_by_user_id BIGINT NULL;
    ALTER TABLE bookings ADD CONSTRAINT fk_bookings_cancelled_by FOREIGN KEY (cancelled_by_user_id) REFERENCES users(id);
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'cancelled_at')
BEGIN
    ALTER TABLE bookings ADD cancelled_at DATETIME2 NULL;
END;
