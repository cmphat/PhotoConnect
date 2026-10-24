-- =============================================================================
-- Migration: V025__create_user_reports.sql
-- Description: Creates user_reports table for Trust & Safety violation reporting.
-- =============================================================================

IF OBJECT_ID('dbo.user_reports', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.user_reports (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        reporter_id BIGINT NOT NULL,
        reported_user_id BIGINT NULL,
        booking_id BIGINT NULL,
        category VARCHAR(50) NOT NULL,
        details NVARCHAR(1500) NOT NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_user_reports_status DEFAULT 'PENDING',
        admin_notes NVARCHAR(1000) NULL,
        resolved_by_admin_id BIGINT NULL,
        resolved_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_user_reports_created_at DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL CONSTRAINT DF_user_reports_updated_at DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT FK_user_reports_reporter FOREIGN KEY (reporter_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_user_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_user_reports_booking FOREIGN KEY (booking_id) REFERENCES dbo.bookings(id)
    );

    CREATE INDEX IX_user_reports_status ON dbo.user_reports(status);
    CREATE INDEX IX_user_reports_reporter ON dbo.user_reports(reporter_id);
    CREATE INDEX IX_user_reports_booking ON dbo.user_reports(booking_id);
END
