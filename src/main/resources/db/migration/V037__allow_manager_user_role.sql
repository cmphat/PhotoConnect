-- PhotoConnect V5: Allow MANAGER role in users table
-- Removes legacy system-named or custom CHECK constraints on dbo.users(role)
-- and creates an explicit named constraint allowing ('CUSTOMER', 'PHOTOGRAPHER', 'ADMIN', 'MANAGER').

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.users', 'U') IS NULL
    THROW 51010, 'Cannot update user role constraint: dbo.users does not exist.', 1;

-- Discover and drop any check constraints on column 'role' of table 'dbo.users'
DECLARE @dropSql nvarchar(max) = N'';

SELECT @dropSql = @dropSql + N'ALTER TABLE dbo.users DROP CONSTRAINT ' + QUOTENAME(cc.name) + N'; '
FROM sys.check_constraints cc
JOIN sys.columns c
  ON c.object_id = cc.parent_object_id
 AND c.column_id = cc.parent_column_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.users')
  AND c.name = 'role';

IF @dropSql <> N''
BEGIN
    EXEC sys.sp_executesql @dropSql;
    PRINT 'Dropped legacy check constraint(s) on dbo.users(role).';
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.users')
      AND name = 'CK_users_role'
)
BEGIN
    ALTER TABLE dbo.users WITH CHECK
        ADD CONSTRAINT CK_users_role
        CHECK (role IN ('CUSTOMER', 'PHOTOGRAPHER', 'ADMIN', 'MANAGER'));
    ALTER TABLE dbo.users CHECK CONSTRAINT CK_users_role;
    PRINT 'Added CK_users_role constraint allowing CUSTOMER, PHOTOGRAPHER, ADMIN, MANAGER.';
END;

COMMIT TRANSACTION;
