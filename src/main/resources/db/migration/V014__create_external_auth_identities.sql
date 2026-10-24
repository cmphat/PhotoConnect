/* TASK-D02: SQL Server mapping from verified external identities to local users. */
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.external_auth_identities', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.external_auth_identities (
        id BIGINT IDENTITY(1,1) NOT NULL,
        user_id BIGINT NOT NULL,
        provider NVARCHAR(30) NOT NULL,
        provider_subject NVARCHAR(255) NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_external_auth_identities_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_external_auth_identities PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_external_auth_identities_user FOREIGN KEY (user_id)
            REFERENCES dbo.users(id) ON DELETE CASCADE,
        CONSTRAINT UQ_external_auth_provider_subject UNIQUE (provider, provider_subject)
    );

    CREATE NONCLUSTERED INDEX IX_external_auth_identities_user_id
        ON dbo.external_auth_identities(user_id);
END;

COMMIT TRANSACTION;
