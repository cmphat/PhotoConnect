/*
  ==============================================================================
  Migration: V031__create_vietqr_payment_and_pricing_breakdown.sql
  Task: PHOTOCONNECT V6 - Mandatory 30% Deposit, VietQR Payment & Booking Integration
  Description:
    1. Adds authoritative price breakdown columns to 'bookings' (base_price,
       night_surcharge, travel_distance_km, travel_surcharge, extras_fee,
       extra_hours, extra_photos, additional_photographer).
    2. Adds provider payment metadata columns to 'deposits' (provider_order_code,
       provider_name, provider_transaction_id, qr_code_url, checkout_url, expires_at).
    3. Creates 'payment_transactions' audit log table for webhook events with
       unique constraint on (provider_name, external_transaction_id) to prevent replay.
  Compatible with: Microsoft SQL Server 2019+
  Forward-only, non-destructive, idempotent.
  ==============================================================================
*/

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Extend dbo.bookings with authoritative pricing breakdown snapshot fields
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.bookings', 'base_price') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD base_price DECIMAL(18,2) NULL;
    END;

    IF COL_LENGTH('dbo.bookings', 'night_surcharge') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD night_surcharge DECIMAL(18,2) NOT NULL CONSTRAINT DF_bookings_night_surcharge DEFAULT 0.00;
    END;

    IF COL_LENGTH('dbo.bookings', 'travel_distance_km') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD travel_distance_km INT NOT NULL CONSTRAINT DF_bookings_travel_distance_km DEFAULT 0;
    END;

    IF COL_LENGTH('dbo.bookings', 'travel_surcharge') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD travel_surcharge DECIMAL(18,2) NOT NULL CONSTRAINT DF_bookings_travel_surcharge DEFAULT 0.00;
    END;

    IF COL_LENGTH('dbo.bookings', 'extras_fee') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD extras_fee DECIMAL(18,2) NOT NULL CONSTRAINT DF_bookings_extras_fee DEFAULT 0.00;
    END;

    IF COL_LENGTH('dbo.bookings', 'extra_hours') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD extra_hours INT NOT NULL CONSTRAINT DF_bookings_extra_hours DEFAULT 0;
    END;

    IF COL_LENGTH('dbo.bookings', 'extra_photos') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD extra_photos INT NOT NULL CONSTRAINT DF_bookings_extra_photos DEFAULT 0;
    END;

    IF COL_LENGTH('dbo.bookings', 'additional_photographer') IS NULL
    BEGIN
        ALTER TABLE dbo.bookings ADD additional_photographer BIT NOT NULL CONSTRAINT DF_bookings_additional_photographer DEFAULT 0;
    END;
END;

-- 2. Extend dbo.deposits with provider integration metadata
IF OBJECT_ID('dbo.deposits', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.deposits', 'provider_order_code') IS NULL
    BEGIN
        ALTER TABLE dbo.deposits ADD provider_order_code BIGINT NULL;
    END;

    IF COL_LENGTH('dbo.deposits', 'provider_name') IS NULL
    BEGIN
        ALTER TABLE dbo.deposits ADD provider_name NVARCHAR(50) NULL;
    END;

    IF COL_LENGTH('dbo.deposits', 'provider_transaction_id') IS NULL
    BEGIN
        ALTER TABLE dbo.deposits ADD provider_transaction_id NVARCHAR(100) NULL;
    END;

    IF COL_LENGTH('dbo.deposits', 'qr_code_url') IS NULL
    BEGIN
        ALTER TABLE dbo.deposits ADD qr_code_url NVARCHAR(1000) NULL;
    END;

    IF COL_LENGTH('dbo.deposits', 'checkout_url') IS NULL
    BEGIN
        ALTER TABLE dbo.deposits ADD checkout_url NVARCHAR(1000) NULL;
    END;

    IF COL_LENGTH('dbo.deposits', 'expires_at') IS NULL
    BEGIN
        ALTER TABLE dbo.deposits ADD expires_at DATETIME2 NULL;
    END;
END;

-- 3. Create dbo.payment_transactions audit and deduplication table
IF OBJECT_ID('dbo.payment_transactions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.payment_transactions (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_payment_transactions PRIMARY KEY,
        deposit_id BIGINT NOT NULL,
        provider_name NVARCHAR(50) NOT NULL,
        external_transaction_id NVARCHAR(100) NOT NULL,
        amount DECIMAL(18,2) NOT NULL,
        currency NVARCHAR(10) NOT NULL CONSTRAINT DF_payment_transactions_currency DEFAULT 'VND',
        payment_reference NVARCHAR(100) NOT NULL,
        raw_payload NVARCHAR(MAX) NULL,
        status NVARCHAR(30) NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_payment_transactions_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_payment_transactions_deposit FOREIGN KEY (deposit_id) REFERENCES dbo.deposits(id) ON DELETE CASCADE
    );

    CREATE UNIQUE NONCLUSTERED INDEX UQ_payment_transactions_provider_ext_id
        ON dbo.payment_transactions(provider_name, external_transaction_id);

    CREATE NONCLUSTERED INDEX IX_payment_transactions_deposit_id
        ON dbo.payment_transactions(deposit_id);

    CREATE NONCLUSTERED INDEX IX_payment_transactions_payment_reference
        ON dbo.payment_transactions(payment_reference);
END;

COMMIT TRANSACTION;
