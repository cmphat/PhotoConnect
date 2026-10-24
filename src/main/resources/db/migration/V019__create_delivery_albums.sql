-- =============================================================================
-- Migration: V019__create_delivery_albums.sql
-- Description: Creates delivery_albums and delivered_photos tables for finished photography delivery.
-- Compatibility: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- =============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.delivery_albums', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.delivery_albums (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL CONSTRAINT UQ_delivery_albums_booking UNIQUE,
        status VARCHAR(50) NOT NULL CONSTRAINT DF_delivery_albums_status DEFAULT 'DRAFT',
        notes NVARCHAR(1000) NULL,
        delivered_at DATETIME2 NULL,
        accepted_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_delivery_albums_created DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_delivery_albums_booking FOREIGN KEY (booking_id)
            REFERENCES dbo.bookings(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_delivery_albums_booking ON dbo.delivery_albums(booking_id);
    CREATE INDEX idx_delivery_albums_status ON dbo.delivery_albums(status);
END;

IF OBJECT_ID('dbo.delivered_photos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.delivered_photos (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        album_id BIGINT NOT NULL,
        cloudinary_public_id VARCHAR(255) NOT NULL,
        secure_url VARCHAR(1000) NOT NULL,
        resource_type VARCHAR(50) NOT NULL CONSTRAINT DF_delivered_photos_res_type DEFAULT 'image',
        file_name NVARCHAR(255) NULL,
        file_size_bytes BIGINT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_delivered_photos_created DEFAULT SYSUTCDATETIME(),
        CONSTRAINT fk_delivered_photos_album FOREIGN KEY (album_id)
            REFERENCES dbo.delivery_albums(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_delivered_photos_album ON dbo.delivered_photos(album_id);
END;

COMMIT TRANSACTION;
