-- V036: Platform Fee Ledger & System Configuration
-- 10% platform fee on completed bookings, distinct from 30% customer escrow deposit.
-- Idempotent, separate debt ledger for photographers.

CREATE TABLE system_settings (
    setting_key VARCHAR(50) NOT NULL PRIMARY KEY,
    setting_value NVARCHAR(255) NOT NULL,
    description NVARCHAR(255),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

INSERT INTO system_settings (setting_key, setting_value, description)
VALUES ('PLATFORM_FEE_RATE', '0.10', 'Default platform fee rate applied to completed bookings (e.g. 0.10 = 10%)');
GO

CREATE TABLE platform_fees (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    photographer_id BIGINT NOT NULL,
    photographer_user_id BIGINT NOT NULL,
    booking_amount DECIMAL(18,2) NOT NULL,
    fee_rate DECIMAL(5,4) NOT NULL,
    fee_amount DECIMAL(18,2) NOT NULL,
    paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    outstanding_amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    transaction_code VARCHAR(100),
    payment_method VARCHAR(50),
    notes NVARCHAR(500),
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    paid_at DATETIME2,
    CONSTRAINT FK_platform_fees_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT FK_platform_fees_photographer FOREIGN KEY (photographer_id) REFERENCES photographer_profiles(id),
    CONSTRAINT FK_platform_fees_user FOREIGN KEY (photographer_user_id) REFERENCES users(id),
    CONSTRAINT UQ_platform_fees_booking UNIQUE (booking_id)
);
GO

CREATE INDEX IX_platform_fees_photographer_user_id ON platform_fees(photographer_user_id);
GO
CREATE INDEX IX_platform_fees_status ON platform_fees(status);
GO
CREATE INDEX IX_platform_fees_created_at ON platform_fees(created_at);
GO
