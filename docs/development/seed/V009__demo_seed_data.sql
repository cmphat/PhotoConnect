-- ==============================================================================
-- PhotoConnect – Development & Demo Seed Data
-- File: docs/development/seed/V009__demo_seed_data.sql
-- Task: TASK-028 Development and Demo Seed Data
-- Dialect: Microsoft SQL Server 2019+
--
-- SAFETY RULES:
-- 1. STRICTLY NON-DESTRUCTIVE: Never drops tables or truncates existing data.
-- 2. FULLY IDEMPOTENT: Uses IF NOT EXISTS guards for all records. Safe to rerun.
-- 3. DEV CREDENTIALS ONLY: All accounts use password: password123
--    BCrypt Hash: $2a$10$MswCCZEoO3Q91enCgvZI.OWqz34HhwHAdFEnJ22YO02gJx07Siqqy
-- ==============================================================================

USE PhotoConnect;
GO

SET NOCOUNT ON;

PRINT 'Starting PhotoConnect demo seed execution...';

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. USERS: Admin, Customers, and Photographers
-- ─────────────────────────────────────────────────────────────────────────────

DECLARE @DevPassword NVARCHAR(255) = N'$2a$10$MswCCZEoO3Q91enCgvZI.OWqz34HhwHAdFEnJ22YO02gJx07Siqqy'; -- password123
DECLARE @Now DATETIME2 = SYSUTCDATETIME();

-- Admin Account
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'admin@photoconnect.test')
BEGIN
    INSERT INTO dbo.users (email, password, full_name, phone, role, status, created_at, updated_at)
    VALUES ('admin@photoconnect.test', @DevPassword, N'System Administrator', '0901000001', 'ADMIN', 'ACTIVE', @Now, @Now);
    PRINT 'Created user: admin@photoconnect.test';
END

-- Customer Accounts
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'customer1@photoconnect.test')
BEGIN
    INSERT INTO dbo.users (email, password, full_name, phone, role, status, created_at, updated_at)
    VALUES ('customer1@photoconnect.test', @DevPassword, N'Nguyen Van An', '0902000001', 'CUSTOMER', 'ACTIVE', @Now, @Now);
    PRINT 'Created user: customer1@photoconnect.test';
END

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'customer2@photoconnect.test')
BEGIN
    INSERT INTO dbo.users (email, password, full_name, phone, role, status, created_at, updated_at)
    VALUES ('customer2@photoconnect.test', @DevPassword, N'Le Thi Binh', '0902000002', 'CUSTOMER', 'ACTIVE', @Now, @Now);
    PRINT 'Created user: customer2@photoconnect.test';
END

-- 14 Approved Photographers + 1 Pending Photographer
-- Photographers 1 to 5: Hanoi
-- Photographers 6 to 10: Ho Chi Minh City
-- Photographers 11 to 14: Da Nang
-- Photographer 15: Hanoi (PENDING approval for admin demo)

DECLARE @Photographers TABLE (
    Email NVARCHAR(255),
    FullName NVARCHAR(150),
    Phone NVARCHAR(20),
    DisplayName NVARCHAR(150),
    City NVARCHAR(100),
    ExpYears INT,
    PriceFrom DECIMAL(18,2),
    Bio NVARCHAR(MAX),
    Status VARCHAR(30)
);

INSERT INTO @Photographers (Email, FullName, Phone, DisplayName, City, ExpYears, PriceFrom, Bio, Status)
VALUES
-- Hanoi (5 Approved)
('photographer1@photoconnect.test', N'Tran Minh Quang', '0903000001', N'Minh Quang Editorial', N'Hanoi', 8, 2500000.00,
 N'Contemporary visual storyteller specializing in cinematic fashion editorials, portraiture, and high-fashion spreads. Based in Hoan Kiem, Hanoi.', 'APPROVED'),
('photographer2@photoconnect.test', N'Vu Hoang Nam', '0903000002', N'Nam Vu Fine Art', N'Hanoi', 5, 1800000.00,
 N'Fine art and modern street photography blending classical black-and-white grain with natural northern light.', 'APPROVED'),
('photographer3@photoconnect.test', N'Doan Thu Trang', '0903000003', N'Thu Trang Studio', N'Hanoi', 3, 1200000.00,
 N'Minimalist studio portraits, lookbooks, and clean commercial imagery with soft studio lighting.', 'APPROVED'),
('photographer4@photoconnect.test', N'Pham Quoc Bao', '0903000004', N'Bao Pham Architecture', N'Hanoi', 10, 3500000.00,
 N'Specializing in architectural heritage, interior aesthetics, and geometric commercial structures across the capital.', 'APPROVED'),
('photographer5@photoconnect.test', N'Bui Thanh Tung', '0903000005', N'Tung Bui Moments', N'Hanoi', 2, 800000.00,
 N'Young, energetic visual artist documenting youth culture, graduation sessions, and intimate coffeehouse lifestyle.', 'APPROVED'),

-- Ho Chi Minh City (5 Approved)
('photographer6@photoconnect.test', N'Nguyen Hoang Long', '0903000006', N'Long Nguyen Visuals', N'Ho Chi Minh City', 12, 4500000.00,
 N'Award-winning commercial director and luxury wedding photographer based in District 1. High contrast, vivid editorial mood.', 'APPROVED'),
('photographer7@photoconnect.test', N'Tran Thi Mai', '0903000007', N'Mai Tran Portraits', N'Ho Chi Minh City', 6, 2000000.00,
 N'Warm golden hour portraiture, candid couples, and romantic elopements across Saigon rooftops and gardens.', 'APPROVED'),
('photographer8@photoconnect.test', N'Dinh Van Phuc', '0903000008', N'Phuc Dinh Culinary', N'Ho Chi Minh City', 4, 1500000.00,
 N'Gastronomy and high-end restaurant food photography. Capturing textures, vibrant ingredients, and culinary narratives.', 'APPROVED'),
('photographer9@photoconnect.test', N'Hoang Duc Thang', '0903000009', N'Thang Hoang Street', N'Ho Chi Minh City', 7, 2200000.00,
 N'Documentary storyteller capturing the pulse of Saigon: vintage alleys, nightlife, neon reflections, and candid humanity.', 'APPROVED'),
('photographer10@photoconnect.test', N'Ngo Bao Chau', '0903000010', N'Chau Ngo Studio', N'Ho Chi Minh City', 1, 500000.00,
 N'Emerging talent focused on creative studio beauty, personal branding headshots, and experimental color styling.', 'APPROVED'),

-- Da Nang (4 Approved)
('photographer11@photoconnect.test', N'Cao Xuan Bach', '0903000011', N'Bach Cao Coastal', N'Da Nang', 9, 3000000.00,
 N'Seascape and destination wedding photographer covering Central Vietnam, Son Tra Peninsula, and historic Hoi An.', 'APPROVED'),
('photographer12@photoconnect.test', N'Phan My Linh', '0903000012', N'My Linh Lifestyle', N'Da Nang', 4, 1400000.00,
 N'Natural light storyteller capturing resort vacations, family portraits, and tranquil beach mornings.', 'APPROVED'),
('photographer13@photoconnect.test', N'Luu Tuan Anh', '0903000013', N'Tuan Anh Drone & Nature', N'Da Nang', 6, 2800000.00,
 N'Aerial landscape panoramas, architectural resorts, and dramatic mountain-to-sea perspective compositions.', 'APPROVED'),
('photographer14@photoconnect.test', N'Trinh Thu Hang', '0903000014', N'Hang Trinh Vintage', N'Da Nang', 3, 1000000.00,
 N'Nostalgic 35mm film aesthetic, warm earthy tones, and soulful acoustic portraits in Hoi An ancient town.', 'APPROVED'),

-- Hanoi (1 Pending Photographer for Admin Approval Demo)
('photographer15@photoconnect.test', N'Dang Gia Huy', '0903000015', N'Gia Huy Experimental', N'Hanoi', 2, 900000.00,
 N'Experimental film and abstract night photography looking for verification on PhotoConnect.', 'PENDING');

-- Insert Users and Photographer Profiles
DECLARE @Email NVARCHAR(255), @FullName NVARCHAR(150), @Phone NVARCHAR(20);
DECLARE @DisplayName NVARCHAR(150), @City NVARCHAR(100), @ExpYears INT, @PriceFrom DECIMAL(18,2);
DECLARE @Bio NVARCHAR(MAX), @Status VARCHAR(30);

DECLARE photo_cursor CURSOR LOCAL FAST_FORWARD FOR
SELECT Email, FullName, Phone, DisplayName, City, ExpYears, PriceFrom, Bio, Status FROM @Photographers;

OPEN photo_cursor;
FETCH NEXT FROM photo_cursor INTO @Email, @FullName, @Phone, @DisplayName, @City, @ExpYears, @PriceFrom, @Bio, @Status;

WHILE @@FETCH_STATUS = 0
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = @Email)
    BEGIN
        INSERT INTO dbo.users (email, password, full_name, phone, role, status, created_at, updated_at)
        VALUES (@Email, @DevPassword, @FullName, @Phone, 'PHOTOGRAPHER', 'ACTIVE', @Now, @Now);
        PRINT 'Created user: ' + @Email;
    END

    DECLARE @UserId BIGINT = (SELECT id FROM dbo.users WHERE email = @Email);

    IF NOT EXISTS (SELECT 1 FROM dbo.photographer_profiles WHERE user_id = @UserId)
    BEGIN
        INSERT INTO dbo.photographer_profiles (
            user_id, display_name, bio, city, experience_years, price_from,
            verification_status, average_rating, review_count, created_at, updated_at
        )
        VALUES (
            @UserId, @DisplayName, @Bio, @City, @ExpYears, @PriceFrom,
            @Status, 0.0, 0, @Now, @Now
        );
        PRINT 'Created profile for: ' + @DisplayName;
    END

    FETCH NEXT FROM photo_cursor INTO @Email, @FullName, @Phone, @DisplayName, @City, @ExpYears, @PriceFrom, @Bio, @Status;
END

CLOSE photo_cursor;
DEALLOCATE photo_cursor;

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. PORTFOLIO IMAGES: Elegant curated photography for approved artists
-- ─────────────────────────────────────────────────────────────────────────────

DECLARE @P1ProfileId BIGINT = (SELECT p.id FROM dbo.photographer_profiles p JOIN dbo.users u ON p.user_id = u.id WHERE u.email = 'photographer1@photoconnect.test');
DECLARE @P2ProfileId BIGINT = (SELECT p.id FROM dbo.photographer_profiles p JOIN dbo.users u ON p.user_id = u.id WHERE u.email = 'photographer2@photoconnect.test');
DECLARE @P6ProfileId BIGINT = (SELECT p.id FROM dbo.photographer_profiles p JOIN dbo.users u ON p.user_id = u.id WHERE u.email = 'photographer6@photoconnect.test');
DECLARE @P11ProfileId BIGINT = (SELECT p.id FROM dbo.photographer_profiles p JOIN dbo.users u ON p.user_id = u.id WHERE u.email = 'photographer11@photoconnect.test');

IF @P1ProfileId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = @P1ProfileId)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, display_order, created_at)
    VALUES
    (@P1ProfileId, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80', 'seed/p1_01', N'High-fashion portrait with sculptural shadows', 0, @Now),
    (@P1ProfileId, 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1200&q=80', 'seed/p1_02', N'Natural daylight editorial in Old Quarter studio', 1, @Now),
    (@P1ProfileId, 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=1200&q=80', 'seed/p1_03', N'Monochrome lookbook for autumn apparel collection', 2, @Now);
    PRINT 'Added portfolio images for Minh Quang Editorial';
END

IF @P6ProfileId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = @P6ProfileId)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, display_order, created_at)
    VALUES
    (@P6ProfileId, 'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1200&q=80', 'seed/p6_01', N'Destination wedding golden hour ceremony in Saigon', 0, @Now),
    (@P6ProfileId, 'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=1200&q=80', 'seed/p6_02', N'Intimate wedding reception details and candlelight banquet', 1, @Now);
    PRINT 'Added portfolio images for Long Nguyen Visuals';
END

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. AVAILABILITY: Blocked dates for demo photographer 1
-- ─────────────────────────────────────────────────────────────────────────────

IF @P1ProfileId IS NOT NULL
BEGIN
    DECLARE @BlockedDate DATE = DATEADD(DAY, 7, CAST(GETDATE() AS DATE));
    IF NOT EXISTS (SELECT 1 FROM dbo.photographer_unavailable_dates WHERE photographer_profile_id = @P1ProfileId AND unavailable_date = @BlockedDate)
    BEGIN
        INSERT INTO dbo.photographer_unavailable_dates (photographer_profile_id, unavailable_date, reason)
        VALUES (@P1ProfileId, @BlockedDate, N'Commercial shoot booked off-platform');
        PRINT 'Added unavailable date for photographer 1';
    END
END

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. BOOKINGS & DEPOSIT & REVIEW: End-to-End lifecycle demonstration
-- ─────────────────────────────────────────────────────────────────────────────

DECLARE @Cust1Id BIGINT = (SELECT id FROM dbo.users WHERE email = 'customer1@photoconnect.test');
DECLARE @Cust2Id BIGINT = (SELECT id FROM dbo.users WHERE email = 'customer2@photoconnect.test');

-- 4.1 COMPLETED booking with Review & Paid Deposit (Minh Quang & Customer 1)
IF @Cust1Id IS NOT NULL AND @P1ProfileId IS NOT NULL
BEGIN
    DECLARE @CompletedBookingDate DATE = DATEADD(DAY, -5, CAST(GETDATE() AS DATE));
    IF NOT EXISTS (SELECT 1 FROM dbo.bookings WHERE customer_id = @Cust1Id AND photographer_profile_id = @P1ProfileId AND status = 'COMPLETED')
    BEGIN
        INSERT INTO dbo.bookings (customer_id, photographer_profile_id, booking_date, booking_time, location, notes, agreed_price, status, created_at, updated_at)
        VALUES (@Cust1Id, @P1ProfileId, @CompletedBookingDate, '14:00:00', N'Studio LightSpace, Hoan Kiem, Hanoi', N'Lookbook photoshoot for indie apparel brand', 2500000.00, 'COMPLETED', DATEADD(DAY, -10, @Now), @Now);

        DECLARE @B1Id BIGINT = SCOPE_IDENTITY();
        PRINT 'Created COMPLETED booking: ' + CAST(@B1Id AS VARCHAR(20));

        -- Add Deposit (PAID)
        INSERT INTO dbo.deposits (booking_id, amount, status, payment_reference, paid_at, created_at, updated_at)
        VALUES (@B1Id, 750000.00, 'PAID', 'DEV_PAY_SIM_001', DATEADD(DAY, -9, @Now), DATEADD(DAY, -10, @Now), DATEADD(DAY, -9, @Now));

        -- Add Review
        INSERT INTO dbo.reviews (booking_id, customer_id, photographer_profile_id, rating, comment, status, created_at, updated_at)
        VALUES (@B1Id, @Cust1Id, @P1ProfileId, 5, N'Exceptional visual direction and lighting! The photos were delivered ahead of schedule and perfectly captured the aesthetic we envisioned.', 'VISIBLE', DATEADD(DAY, -4, @Now), DATEADD(DAY, -4, @Now));

        -- Update Photographer 1 Rating Stats
        UPDATE dbo.photographer_profiles
        SET average_rating = 5.0, review_count = 1
        WHERE id = @P1ProfileId;

        -- Add Chat Messages
        DECLARE @P1UserId BIGINT = (SELECT user_id FROM dbo.photographer_profiles WHERE id = @P1ProfileId);
        INSERT INTO dbo.messages (booking_id, sender_id, receiver_id, content, is_read, sent_at)
        VALUES
        (@B1Id, @Cust1Id, @P1UserId, N'Hi Minh Quang, we are looking forward to our shoot this weekend! Could we prepare 3 different wardrobe changes?', 1, DATEADD(DAY, -8, @Now)),
        (@B1Id, @P1UserId, @Cust1Id, N'Hello An! 3 wardrobe changes will fit smoothly within our allocated timeframe. See you on Saturday at Studio LightSpace.', 1, DATEADD(DAY, -8, @Now));
    END
END

-- 4.2 ACCEPTED booking with Pending Deposit (Nam Vu & Customer 2)
IF @Cust2Id IS NOT NULL AND @P2ProfileId IS NOT NULL
BEGIN
    DECLARE @AcceptedBookingDate DATE = DATEADD(DAY, 3, CAST(GETDATE() AS DATE));
    IF NOT EXISTS (SELECT 1 FROM dbo.bookings WHERE customer_id = @Cust2Id AND photographer_profile_id = @P2ProfileId AND status = 'ACCEPTED')
    BEGIN
        INSERT INTO dbo.bookings (customer_id, photographer_profile_id, booking_date, booking_time, location, notes, agreed_price, status, created_at, updated_at)
        VALUES (@Cust2Id, @P2ProfileId, @AcceptedBookingDate, '09:00:00', N'Hanoi Botanical Garden', N'Fine art portrait session in morning light', 1800000.00, 'ACCEPTED', DATEADD(DAY, -1, @Now), @Now);

        DECLARE @B2Id BIGINT = SCOPE_IDENTITY();
        PRINT 'Created ACCEPTED booking: ' + CAST(@B2Id AS VARCHAR(20));

        -- Add Deposit (PENDING)
        INSERT INTO dbo.deposits (booking_id, amount, status, payment_reference, paid_at, created_at, updated_at)
        VALUES (@B2Id, 540000.00, 'PENDING', NULL, NULL, @Now, @Now);
    END
END

-- 4.3 PENDING booking awaiting photographer response (Long Nguyen & Customer 1)
IF @Cust1Id IS NOT NULL AND @P6ProfileId IS NOT NULL
BEGIN
    DECLARE @PendingBookingDate DATE = DATEADD(DAY, 14, CAST(GETDATE() AS DATE));
    IF NOT EXISTS (SELECT 1 FROM dbo.bookings WHERE customer_id = @Cust1Id AND photographer_profile_id = @P6ProfileId AND status = 'PENDING')
    BEGIN
        INSERT INTO dbo.bookings (customer_id, photographer_profile_id, booking_date, booking_time, location, notes, agreed_price, status, created_at, updated_at)
        VALUES (@Cust1Id, @P6ProfileId, @PendingBookingDate, '16:30:00', N'Landmark 81 Skyview, District Binh Thanh, HCMC', N'Sunset pre-wedding portrait session', 4500000.00, 'PENDING', @Now, @Now);
        PRINT 'Created PENDING booking for Long Nguyen';
    END
END

PRINT 'PhotoConnect demo seed execution completed successfully!';
GO
