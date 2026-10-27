-- Reconcile the optimistic-lock column required by Booking.version.
-- V027 was already recorded as applied in some databases before this column
-- was included in that script. This forward-only migration repairs those
-- databases without changing any previously applied migration.

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.bookings', 'U') IS NULL
    THROW 51000, 'Cannot add optimistic-lock version: dbo.bookings does not exist.', 1;

IF COL_LENGTH('dbo.bookings', 'version') IS NULL
BEGIN
    ALTER TABLE dbo.bookings ADD version BIGINT NULL;
    -- Dynamic SQL creates a new compilation scope after the column exists.
    EXEC sys.sp_executesql N'UPDATE dbo.bookings SET version = 0 WHERE version IS NULL;';
    EXEC sys.sp_executesql N'ALTER TABLE dbo.bookings ALTER COLUMN version BIGINT NOT NULL;';
END
ELSE
BEGIN
    IF EXISTS (
        SELECT 1
        FROM sys.columns c
        JOIN sys.types t ON c.user_type_id = t.user_type_id
        WHERE c.object_id = OBJECT_ID('dbo.bookings')
          AND c.name = 'version'
          AND t.name <> 'bigint'
    )
        THROW 51001, 'Cannot reconcile dbo.bookings.version: expected BIGINT.', 1;

    EXEC sys.sp_executesql N'UPDATE dbo.bookings SET version = 0 WHERE version IS NULL;';

    IF EXISTS (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID('dbo.bookings')
          AND name = 'version'
          AND is_nullable = 1
    )
        EXEC sys.sp_executesql N'ALTER TABLE dbo.bookings ALTER COLUMN version BIGINT NOT NULL;';
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints dc
    JOIN sys.columns c
      ON c.object_id = dc.parent_object_id
     AND c.column_id = dc.parent_column_id
    WHERE dc.parent_object_id = OBJECT_ID('dbo.bookings')
      AND c.name = 'version'
)
    EXEC sys.sp_executesql
        N'ALTER TABLE dbo.bookings ADD CONSTRAINT DF_bookings_version DEFAULT (0) FOR version;';

COMMIT TRANSACTION;
