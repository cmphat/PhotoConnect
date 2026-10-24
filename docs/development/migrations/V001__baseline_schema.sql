-- ==============================================================================
-- Migration: V001__baseline_schema.sql
-- Description: Baseline schema definition for PhotoConnect core domain tables:
--              users, photographer_profiles, portfolio_images,
--              photographer_unavailable_dates, bookings, deposits,
--              reviews, messages.
-- Compatible with: Microsoft SQL Server 2019+
-- Forward-only, non-destructive, idempotent.
-- ==============================================================================

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. users table
IF OBJECT_ID('dbo.users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL,
        email NVARCHAR(255) NOT NULL,
        password NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(150) NOT NULL,
        phone NVARCHAR(20) NULL,
        role VARCHAR(30) NOT NULL,
        status VARCHAR(30) NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT PK_users PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_users_email UNIQUE (email)
    );

    CREATE NONCLUSTERED INDEX IX_users_email ON dbo.users(email);
    CREATE NONCLUSTERED INDEX IX_users_role_status ON dbo.users(role, status);
    PRINT 'Created table users.';
END;

-- 2. photographer_profiles table
IF OBJECT_ID('dbo.photographer_profiles', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.photographer_profiles (
        id BIGINT IDENTITY(1,1) NOT NULL,
        user_id BIGINT NOT NULL,
        display_name NVARCHAR(150) NOT NULL,
        bio NVARCHAR(MAX) NULL,
        city NVARCHAR(100) NULL,
        experience_years INT NULL,
        price_from DECIMAL(18,2) NULL,
        verification_status VARCHAR(30) NOT NULL CONSTRAINT DF_photographer_profiles_verification DEFAULT 'PENDING',
        average_rating FLOAT NOT NULL CONSTRAINT DF_photographer_profiles_rating DEFAULT 0.0,
        review_count INT NOT NULL CONSTRAINT DF_photographer_profiles_review_count DEFAULT 0,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_photographer_profiles_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT PK_photographer_profiles PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_photographer_profiles_user_id UNIQUE (user_id),
        CONSTRAINT FK_photographer_profiles_user FOREIGN KEY (user_id)
            REFERENCES dbo.users(id) ON DELETE CASCADE
    );

    CREATE NONCLUSTERED INDEX IX_photographer_profiles_user_id ON dbo.photographer_profiles(user_id);
    CREATE NONCLUSTERED INDEX IX_photographer_profiles_verification_city ON dbo.photographer_profiles(verification_status, city);
    PRINT 'Created table photographer_profiles.';
END;

-- 3. portfolio_images table
IF OBJECT_ID('dbo.portfolio_images', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.portfolio_images (
        id BIGINT IDENTITY(1,1) NOT NULL,
        photographer_profile_id BIGINT NOT NULL,
        image_url VARCHAR(2048) NOT NULL,
        public_id VARCHAR(512) NOT NULL,
        caption NVARCHAR(500) NULL,
        display_order INT NOT NULL CONSTRAINT DF_portfolio_images_display_order DEFAULT 0,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_portfolio_images_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_portfolio_images PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_portfolio_images_profile FOREIGN KEY (photographer_profile_id)
            REFERENCES dbo.photographer_profiles(id) ON DELETE CASCADE
    );

    CREATE NONCLUSTERED INDEX IX_portfolio_images_profile ON dbo.portfolio_images(photographer_profile_id);
    PRINT 'Created table portfolio_images.';
END;

-- 4. photographer_unavailable_dates table
IF OBJECT_ID('dbo.photographer_unavailable_dates', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.photographer_unavailable_dates (
        id BIGINT IDENTITY(1,1) NOT NULL,
        photographer_profile_id BIGINT NOT NULL,
        unavailable_date DATE NOT NULL,
        reason NVARCHAR(255) NULL,
        CONSTRAINT PK_photographer_unavailable_dates PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_photographer_unavailable_dates_profile FOREIGN KEY (photographer_profile_id)
            REFERENCES dbo.photographer_profiles(id) ON DELETE CASCADE,
        CONSTRAINT UQ_photographer_unavailable_dates UNIQUE (photographer_profile_id, unavailable_date)
    );

    CREATE NONCLUSTERED INDEX IX_unavailable_dates_lookup 
        ON dbo.photographer_unavailable_dates(photographer_profile_id, unavailable_date);
    PRINT 'Created table photographer_unavailable_dates.';
END;

-- 5. bookings table
IF OBJECT_ID('dbo.bookings', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.bookings (
        id BIGINT IDENTITY(1,1) NOT NULL,
        customer_id BIGINT NOT NULL,
        photographer_profile_id BIGINT NOT NULL,
        booking_date DATE NOT NULL,
        booking_time TIME NOT NULL,
        location NVARCHAR(255) NOT NULL,
        notes NVARCHAR(1000) NULL,
        agreed_price DECIMAL(18,2) NOT NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_bookings_status DEFAULT 'PENDING',
        created_at DATETIME2 NOT NULL CONSTRAINT DF_bookings_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT PK_bookings PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_bookings_customer FOREIGN KEY (customer_id)
            REFERENCES dbo.users(id),
        CONSTRAINT FK_bookings_profile FOREIGN KEY (photographer_profile_id)
            REFERENCES dbo.photographer_profiles(id)
    );

    CREATE NONCLUSTERED INDEX IX_bookings_customer_id ON dbo.bookings(customer_id);
    CREATE NONCLUSTERED INDEX IX_bookings_profile_id ON dbo.bookings(photographer_profile_id);
    CREATE NONCLUSTERED INDEX IX_bookings_date_status ON dbo.bookings(booking_date, status);
    PRINT 'Created table bookings.';
END;

-- 6. deposits table
IF OBJECT_ID('dbo.deposits', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.deposits (
        id BIGINT IDENTITY(1,1) NOT NULL,
        booking_id BIGINT NOT NULL,
        amount DECIMAL(18,2) NOT NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_deposits_status DEFAULT 'PENDING',
        payment_reference NVARCHAR(255) NULL,
        paid_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_deposits_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT PK_deposits PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_deposits_booking_id UNIQUE (booking_id),
        CONSTRAINT FK_deposits_booking FOREIGN KEY (booking_id)
            REFERENCES dbo.bookings(id) ON DELETE CASCADE
    );

    CREATE NONCLUSTERED INDEX IX_deposits_booking_id ON dbo.deposits(booking_id);
    PRINT 'Created table deposits.';
END;

-- 7. reviews table
IF OBJECT_ID('dbo.reviews', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.reviews (
        id BIGINT IDENTITY(1,1) NOT NULL,
        booking_id BIGINT NOT NULL,
        customer_id BIGINT NOT NULL,
        photographer_profile_id BIGINT NOT NULL,
        rating INT NOT NULL,
        comment NVARCHAR(1000) NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_reviews_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT PK_reviews PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_reviews_booking_id UNIQUE (booking_id),
        CONSTRAINT FK_reviews_booking FOREIGN KEY (booking_id)
            REFERENCES dbo.bookings(id) ON DELETE CASCADE,
        CONSTRAINT FK_reviews_customer FOREIGN KEY (customer_id)
            REFERENCES dbo.users(id),
        CONSTRAINT FK_reviews_profile FOREIGN KEY (photographer_profile_id)
            REFERENCES dbo.photographer_profiles(id)
    );

    CREATE NONCLUSTERED INDEX IX_reviews_photographer_profile ON dbo.reviews(photographer_profile_id);
    CREATE NONCLUSTERED INDEX IX_reviews_customer ON dbo.reviews(customer_id);
    PRINT 'Created table reviews.';
END;

-- 8. messages table
IF OBJECT_ID('dbo.messages', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.messages (
        id BIGINT IDENTITY(1,1) NOT NULL,
        booking_id BIGINT NOT NULL,
        sender_id BIGINT NOT NULL,
        receiver_id BIGINT NOT NULL,
        content NVARCHAR(2000) NOT NULL,
        is_read BIT NOT NULL CONSTRAINT DF_messages_is_read DEFAULT 0,
        sent_at DATETIME2 NOT NULL CONSTRAINT DF_messages_sent_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_messages PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_messages_booking FOREIGN KEY (booking_id)
            REFERENCES dbo.bookings(id) ON DELETE CASCADE,
        CONSTRAINT FK_messages_sender FOREIGN KEY (sender_id)
            REFERENCES dbo.users(id),
        CONSTRAINT FK_messages_receiver FOREIGN KEY (receiver_id)
            REFERENCES dbo.users(id)
    );

    CREATE NONCLUSTERED INDEX IX_messages_booking_sent_at ON dbo.messages(booking_id, sent_at);
    CREATE NONCLUSTERED INDEX IX_messages_receiver_read ON dbo.messages(receiver_id, is_read);
    PRINT 'Created table messages.';
END;

COMMIT TRANSACTION;
