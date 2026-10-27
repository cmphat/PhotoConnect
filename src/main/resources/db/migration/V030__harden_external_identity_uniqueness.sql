/* Native OIDC hardening: one identity per provider and local account. */
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.external_auth_identities', 'U') IS NULL
BEGIN
    THROW 51030, 'V030 requires dbo.external_auth_identities from V014.', 1;
END;

IF EXISTS (
    SELECT user_id, provider
    FROM dbo.external_auth_identities
    GROUP BY user_id, provider
    HAVING COUNT_BIG(*) > 1
)
BEGIN
    THROW 51031, 'Duplicate external identities must be reviewed before V030 can add the uniqueness constraint.', 1;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE [name] = 'UQ_external_auth_user_provider'
      AND [parent_object_id] = OBJECT_ID('dbo.external_auth_identities')
)
BEGIN
    ALTER TABLE dbo.external_auth_identities
        ADD CONSTRAINT UQ_external_auth_user_provider UNIQUE (user_id, provider);
END;

COMMIT TRANSACTION;
