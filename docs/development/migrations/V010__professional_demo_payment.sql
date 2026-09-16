/*
  TASK-029 - Professional Demo Payment metadata
  SQL Server, forward-only, non-destructive.

  Apply manually in SSMS against the PhotoConnect database before running with
  spring.jpa.hibernate.ddl-auto=validate/none. Existing rows remain valid because
  both columns are nullable.
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH('dbo.deposits', 'payment_method') IS NULL
BEGIN
    ALTER TABLE dbo.deposits ADD payment_method VARCHAR(30) NULL;
END;

IF COL_LENGTH('dbo.deposits', 'failure_reason') IS NULL
BEGIN
    ALTER TABLE dbo.deposits ADD failure_reason NVARCHAR(255) NULL;
END;

COMMIT TRANSACTION;
