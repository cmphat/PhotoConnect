-- =============================================================================
-- Migration: V020__create_notifications.sql
-- Description: Creates notifications table for persistent user notifications.
-- Compatibility: Microsoft SQL Server, PostgreSQL, MySQL
-- =============================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'notifications')
BEGIN
    CREATE TABLE notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        recipient_id BIGINT NOT NULL,
        type VARCHAR(50) NOT NULL,
        title NVARCHAR(255) NOT NULL,
        message NVARCHAR(1000) NOT NULL,
        reference_id BIGINT NULL,
        target_url VARCHAR(500) NULL,
        read_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id)
            REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
    CREATE INDEX idx_notifications_unread ON notifications(recipient_id, read_at);
    CREATE INDEX idx_notifications_created_at ON notifications(created_at);
END;
