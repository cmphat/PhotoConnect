-- PhotoConnect V5: Manager operations, immutable audit trail and support center.
-- Forward-only SQL Server migration. No existing roles or data are rewritten.

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.operational_audit_logs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.operational_audit_logs (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_operational_audit_logs PRIMARY KEY,
        actor_id BIGINT NOT NULL,
        actor_role VARCHAR(30) NOT NULL,
        action VARCHAR(80) NOT NULL,
        target_type VARCHAR(80) NOT NULL,
        target_id VARCHAR(100) NULL,
        outcome VARCHAR(20) NOT NULL,
        summary NVARCHAR(1000) NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_operational_audit_created DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_operational_audit_actor FOREIGN KEY (actor_id) REFERENCES dbo.users(id)
    );
    CREATE INDEX IX_operational_audit_created ON dbo.operational_audit_logs(created_at DESC);
    CREATE INDEX IX_operational_audit_actor ON dbo.operational_audit_logs(actor_id, created_at DESC);
END;

IF OBJECT_ID('dbo.support_tickets', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.support_tickets (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_support_tickets PRIMARY KEY,
        requester_id BIGINT NOT NULL,
        assigned_manager_id BIGINT NULL,
        category VARCHAR(30) NOT NULL,
        subject NVARCHAR(200) NOT NULL,
        description NVARCHAR(2000) NOT NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_support_ticket_status DEFAULT 'OPEN',
        created_at DATETIME2 NOT NULL CONSTRAINT DF_support_ticket_created DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL CONSTRAINT DF_support_ticket_updated DEFAULT SYSUTCDATETIME(),
        resolved_at DATETIME2 NULL,
        CONSTRAINT FK_support_ticket_requester FOREIGN KEY (requester_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_support_ticket_manager FOREIGN KEY (assigned_manager_id) REFERENCES dbo.users(id)
    );
    CREATE INDEX IX_support_ticket_requester ON dbo.support_tickets(requester_id, updated_at DESC);
    CREATE INDEX IX_support_ticket_status ON dbo.support_tickets(status, updated_at DESC);
    CREATE INDEX IX_support_ticket_manager ON dbo.support_tickets(assigned_manager_id, status);
END;

IF OBJECT_ID('dbo.support_ticket_messages', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.support_ticket_messages (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_support_ticket_messages PRIMARY KEY,
        ticket_id BIGINT NOT NULL,
        author_id BIGINT NOT NULL,
        message NVARCHAR(2000) NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_support_ticket_message_created DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_support_ticket_message_ticket FOREIGN KEY (ticket_id) REFERENCES dbo.support_tickets(id),
        CONSTRAINT FK_support_ticket_message_author FOREIGN KEY (author_id) REFERENCES dbo.users(id)
    );
    CREATE INDEX IX_support_ticket_message_ticket ON dbo.support_ticket_messages(ticket_id, created_at);
END;

IF COL_LENGTH('dbo.user_reports', 'assigned_manager_id') IS NULL
    ALTER TABLE dbo.user_reports ADD assigned_manager_id BIGINT NULL;
IF COL_LENGTH('dbo.user_reports', 'investigation_notes') IS NULL
    ALTER TABLE dbo.user_reports ADD investigation_notes NVARCHAR(1500) NULL;
IF COL_LENGTH('dbo.user_reports', 'resolution_reason') IS NULL
    ALTER TABLE dbo.user_reports ADD resolution_reason NVARCHAR(1000) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_user_reports_assigned_manager')
    ALTER TABLE dbo.user_reports ADD CONSTRAINT FK_user_reports_assigned_manager
        FOREIGN KEY (assigned_manager_id) REFERENCES dbo.users(id);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_user_reports_assigned_manager' AND object_id = OBJECT_ID('dbo.user_reports'))
    CREATE INDEX IX_user_reports_assigned_manager ON dbo.user_reports(assigned_manager_id, status);

-- Optimistic locking makes concurrent dispute decisions fail atomically instead
-- of allowing the last writer to silently overwrite the first resolution.
IF COL_LENGTH('dbo.bookings', 'version') IS NULL
    ALTER TABLE dbo.bookings ADD version BIGINT NOT NULL CONSTRAINT DF_bookings_version DEFAULT 0;

-- Ensure FK_saved_photographers_profile does not have multiple cascade paths (SQL Server Error 1785)
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys fk
    JOIN sys.tables t ON fk.parent_object_id = t.object_id
    WHERE t.name = 'saved_photographers' AND fk.name = 'FK_saved_photographers_profile'
      AND fk.delete_referential_action_desc = 'CASCADE'
)
BEGIN
    ALTER TABLE dbo.saved_photographers DROP CONSTRAINT FK_saved_photographers_profile;
    ALTER TABLE dbo.saved_photographers WITH CHECK ADD CONSTRAINT FK_saved_photographers_profile
        FOREIGN KEY (photographer_profile_id) REFERENCES dbo.photographer_profiles(id) ON DELETE NO ACTION;
    PRINT 'Aligned FK_saved_photographers_profile to ON DELETE NO ACTION.';
END;

COMMIT TRANSACTION;

