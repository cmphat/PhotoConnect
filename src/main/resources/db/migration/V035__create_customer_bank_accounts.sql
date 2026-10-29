-- V035: Customer Bank Accounts for refunds and platform payouts
-- Strictly stores bank name, branch, account number and holder name.
-- NEVER stores passwords, PIN, OTP, or CVV.
CREATE TABLE customer_bank_accounts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bank_name NVARCHAR(100) NOT NULL,
    bank_code VARCHAR(50),
    branch_name NVARCHAR(150),
    account_number VARCHAR(50) NOT NULL,
    account_holder_name NVARCHAR(150) NOT NULL,
    is_default BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_customer_bank_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_customer_bank_accounts_user_id ON customer_bank_accounts(user_id);
GO
