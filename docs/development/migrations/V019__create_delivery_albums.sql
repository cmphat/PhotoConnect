-- =============================================================================
-- Migration: V019__create_delivery_albums.sql
-- Description: Creates delivery_albums and delivered_photos tables for finished photography delivery.
-- Compatibility: Microsoft SQL Server, PostgreSQL, MySQL
-- =============================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'delivery_albums')
BEGIN
    CREATE TABLE delivery_albums (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL UNIQUE,
        status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
        notes NVARCHAR(1000) NULL,
        delivered_at DATETIME2 NULL,
        accepted_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_delivery_albums_booking FOREIGN KEY (booking_id)
            REFERENCES bookings(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_delivery_albums_booking ON delivery_albums(booking_id);
    CREATE INDEX idx_delivery_albums_status ON delivery_albums(status);
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'delivered_photos')
BEGIN
    CREATE TABLE delivered_photos (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        album_id BIGINT NOT NULL,
        cloudinary_public_id VARCHAR(255) NOT NULL,
        secure_url VARCHAR(1000) NOT NULL,
        resource_type VARCHAR(50) NOT NULL DEFAULT 'image',
        file_name NVARCHAR(255) NULL,
        file_size_bytes BIGINT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_delivered_photos_album FOREIGN KEY (album_id)
            REFERENCES delivery_albums(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_delivered_photos_album ON delivered_photos(album_id);
END;
