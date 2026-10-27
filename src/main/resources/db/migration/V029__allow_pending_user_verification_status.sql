-- Allow newly registered accounts to remain disabled until their email OTP is verified.
-- The original database constraint was system-named, so discover it from metadata.

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.users', 'U') IS NULL
    THROW 51010, 'Cannot update user status constraint: dbo.users does not exist.', 1;

DECLARE @constraintName sysname;

SELECT TOP (1) @constraintName = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c
  ON c.object_id = cc.parent_object_id
 AND c.column_id = cc.parent_column_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.users')
  AND c.name = 'status';

IF @constraintName IS NOT NULL
BEGIN
    DECLARE @dropConstraintSql nvarchar(max) =
        N'ALTER TABLE dbo.users DROP CONSTRAINT ' + QUOTENAME(@constraintName) + N';';
    EXEC sys.sp_executesql @dropConstraintSql;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.users')
      AND name = 'CK_users_status'
)
BEGIN
    ALTER TABLE dbo.users WITH CHECK
        ADD CONSTRAINT CK_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING_VERIFICATION'));
    ALTER TABLE dbo.users CHECK CONSTRAINT CK_users_status;
END;

COMMIT TRANSACTION;
