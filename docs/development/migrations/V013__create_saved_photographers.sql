/*
  ==============================================================================
  Migration: V013__create_saved_photographers.sql
  Task: TASK-C02 Saved Photographers / Favorites
  Description: Creates saved_photographers table for Customer favorites
  Compatible with: Microsoft SQL Server 2019+
  Forward-only, non-destructive, idempotent.
  ==============================================================================
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.saved_photographers', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.saved_photographers (
        id BIGINT IDENTITY(1,1) NOT NULL,
        customer_id BIGINT NOT NULL,
        photographer_profile_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_saved_photographers_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_saved_photographers PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_saved_photographers_customer FOREIGN KEY (customer_id)
            REFERENCES dbo.users(id) ON DELETE CASCADE,
        CONSTRAINT FK_saved_photographers_profile FOREIGN KEY (photographer_profile_id)
            REFERENCES dbo.photographer_profiles(id) ON DELETE CASCADE,
        CONSTRAINT UQ_saved_photographers_customer_profile UNIQUE (customer_id, photographer_profile_id)
    );

    CREATE NONCLUSTERED INDEX IX_saved_photographers_customer_id
        ON dbo.saved_photographers(customer_id);

    CREATE NONCLUSTERED INDEX IX_saved_photographers_photographer_profile_id
        ON dbo.saved_photographers(photographer_profile_id);

    PRINT 'Created table saved_photographers with unique constraint and indexes.';
END
ELSE
BEGIN
    PRINT 'Table saved_photographers already exists.';
END;

COMMIT TRANSACTION;
