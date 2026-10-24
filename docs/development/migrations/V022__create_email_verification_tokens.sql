-- PhotoConnect V5: Create Email Verification Tokens Table for OTP Authentication
-- Supports: Registration verification, Password reset, and Authenticated email change

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'email_verification_tokens')
BEGIN
    CREATE TABLE email_verification_tokens (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email NVARCHAR(255) NOT NULL,
        token_hash NVARCHAR(255) NOT NULL,
        purpose NVARCHAR(50) NOT NULL,
        target_value NVARCHAR(255) NULL,
        attempt_count INT NOT NULL DEFAULT 0,
        max_attempts INT NOT NULL DEFAULT 5,
        expires_at DATETIME2 NOT NULL,
        used_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX idx_evt_email_purpose ON email_verification_tokens (email, purpose);
    CREATE INDEX idx_evt_expires_at ON email_verification_tokens (expires_at);
END;
