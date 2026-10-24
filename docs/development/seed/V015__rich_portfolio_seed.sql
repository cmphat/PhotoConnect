/*
  PhotoConnect Demo Seed: Rich Photography Portfolio Images
  Provides curated royalty-free photography for all 16 approved photographers
  to populate both the Marketplace covers and the Social Newsfeed.
*/
USE PhotoConnect;
GO

SET NOCOUNT ON;

-- Insert or update portfolio images for each approved photographer (IDs 72 to 87)
-- 1. Profile 72 (Chau Minh Phat - HCM - Editorial/Portrait)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 72 AND is_cover = 1)
BEGIN
    UPDATE dbo.portfolio_images SET is_cover = 1 WHERE id = 37;
END;

-- 2. Profile 73 (Saigon Editorial - HCM - Fashion/Editorial)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 73)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (73, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80', 'unsplash/p73_1', N'Bộ ảnh chân dung ánh sáng tự nhiên studio tại Quận 1, tone màu điện ảnh.', 'PORTRAIT', 1, 1, DATEADD(hour, -2, SYSUTCDATETIME())),
    (73, 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1200&q=80', 'unsplash/p73_2', N'Editorial Fashion Lookbook cho bộ sưu tập Mùa Thu thương hiệu thời trang thiết kế.', 'FASHION', 0, 2, DATEADD(hour, -24, SYSUTCDATETIME()));
END;

-- 3. Profile 74 (Golden Hour Studio - Da Nang - Wedding/Lifestyle)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 74)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (74, 'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1200&q=80', 'unsplash/p74_1', N'Hoàng hôn bãi biển Mỹ Khê - khoảnh khắc ngọt ngào của cặp đôi ngày đính hôn.', 'WEDDING', 1, 1, DATEADD(hour, -4, SYSUTCDATETIME())),
    (74, 'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=1200&q=80', 'unsplash/p74_2', N'Lễ cưới bãi biển phong cách tối giản Rustic ven biển Đà Nẵng.', 'WEDDING', 0, 2, DATEADD(day, -2, SYSUTCDATETIME()));
END;

-- 4. Profile 75 (Northlight Weddings - Hanoi - Wedding Documentary)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 75)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (75, 'https://images.unsplash.com/photo-1606800052052-a08af7148866?auto=format&fit=crop&w=1200&q=80', 'unsplash/p75_1', N'Phóng sự ngày cưới truyền thống tại Hà Nội. Nụ cười hạnh phúc của cô dâu.', 'WEDDING', 1, 1, DATEADD(hour, -6, SYSUTCDATETIME())),
    (75, 'https://images.unsplash.com/photo-1583939003579-730e3918a45a?auto=format&fit=crop&w=1200&q=80', 'unsplash/p75_2', N'Khoảnh khắc trao nhẫn thiêng liêng dưới vòm nhà thờ cổ.', 'WEDDING', 0, 2, DATEADD(day, -3, SYSUTCDATETIME()));
END;

-- 5. Profile 76 (Maison Portraits - HCM - Portrait/Studio)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 76)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (76, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=1200&q=80', 'unsplash/p76_1', N'Chân dung nghệ sĩ đương đại, phong cách đen trắng tương phản cao.', 'PORTRAIT', 1, 1, DATEADD(hour, -8, SYSUTCDATETIME())),
    (76, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=1200&q=80', 'unsplash/p76_2', N'Personal Branding Profile cho Founder công nghệ.', 'PORTRAIT', 0, 2, DATEADD(day, -4, SYSUTCDATETIME()));
END;

-- 6. Profile 77 (Coastal Frames - Nha Trang - Travel/Lifestyle)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 77)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (77, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 'unsplash/p77_1', N'Bình minh trên biển Nha Trang, bắt trọn từng vệt sóng và ánh nắng vàng óng.', 'TRAVEL', 1, 1, DATEADD(hour, -10, SYSUTCDATETIME())),
    (77, 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1200&q=80', 'unsplash/p77_2', N'Hành trình khám phá cung đường ven biển Khánh Hòa.', 'LIFESTYLE', 0, 2, DATEADD(day, -5, SYSUTCDATETIME()));
END;

-- 7. Profile 78 (Hue Story House - Hue - Cultural/Heritage)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 78)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (78, 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1200&q=80', 'unsplash/p78_1', N'Tà áo dài trắng bên bờ sông Hương chiều tím Huế mộng mơ.', 'PORTRAIT', 1, 1, DATEADD(hour, -12, SYSUTCDATETIME()));
END;

-- 8. Profile 79 (Urban Grain - Hanoi - Street/Architecture)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 79)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (79, 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=1200&q=80', 'unsplash/p79_1', N'Nhịp sống phố cổ Hà Nội lúc 6h sáng với ánh nắng xiên qua tán bàng.', 'STREET', 1, 1, DATEADD(hour, -15, SYSUTCDATETIME())),
    (79, 'https://images.unsplash.com/photo-1477959858617-67f30bc75b82?auto=format&fit=crop&w=1200&q=80', 'unsplash/p79_2', N'Góc phố hiện đại giao thoa hoài niệm qua lăng kính máy film 35mm.', 'ARCHITECTURE', 0, 2, DATEADD(day, -1, SYSUTCDATETIME()));
END;

-- 9. Profile 80 (Mekong Moments - Can Tho - Family/Lifestyle)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 80)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (80, 'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=1200&q=80', 'unsplash/p80_1', N'Khoảnh khắc gia đình quây quần trên chợ nổi Cái Răng rực rỡ sắc màu.', 'LIFESTYLE', 1, 1, DATEADD(hour, -18, SYSUTCDATETIME()));
END;

-- 10. Profile 81 (Pine & Mist - Da Lat - Romantic/Wedding)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 81)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (81, 'https://images.unsplash.com/photo-1465495976277-4387d4b0b4c6?auto=format&fit=crop&w=1200&q=80', 'unsplash/p81_1', N'Sương sớm đồi thông Đà Lạt, tình yêu bình yên trong veo giữa núi rừng.', 'WEDDING', 1, 1, DATEADD(hour, -1, SYSUTCDATETIME())),
    (81, 'https://images.unsplash.com/photo-1469371670807-013ccf25f16a?auto=format&fit=crop&w=1200&q=80', 'unsplash/p81_2', N'Khoảnh khắc khiêu vũ dưới mưa rừng thông của cặp đôi từ Sài Gòn.', 'WEDDING', 0, 2, DATEADD(day, -2, SYSUTCDATETIME()));
END;

-- 11. Profile 82 (Little Light Co. - Da Nang - Graduation/Youth)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 82)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (82, 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=80', 'unsplash/p82_1', N'Kỷ yếu thanh xuân rực rỡ của nhóm bạn thân dưới tán phượng vĩ.', 'PORTRAIT', 1, 1, DATEADD(hour, -20, SYSUTCDATETIME()));
END;

-- 12. Profile 83 (Frame Foundry - HCM - Commercial/Product)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 83)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (83, 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=1200&q=80', 'unsplash/p83_1', N'Chiến dịch quảng bá đồng hồ cơ khí cao cấp, ánh sáng studio kỹ thuật cao.', 'COMMERCIAL', 1, 1, DATEADD(hour, -22, SYSUTCDATETIME()));
END;

-- 13. Profile 84 (Quiet Vows - Hanoi - Elopement/Intimate)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 84)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (84, 'https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=1200&q=80', 'unsplash/p84_1', N'Lễ cưới riêng tư trên tầng thượng lộng gió ngắm toàn cảnh hồ Tây.', 'WEDDING', 1, 1, DATEADD(hour, -5, SYSUTCDATETIME()));
END;

-- 14. Profile 85 (Event Current - Hai Phong - Event/Concert)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 85)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (85, 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=80', 'unsplash/p85_1', N'Đại nhạc hội ánh sáng EDM ngoài trời, bắt trọn từng giọt cảm xúc bùng nổ.', 'EVENT', 1, 1, DATEADD(hour, -9, SYSUTCDATETIME()));
END;

-- 15. Profile 86 (Soft Focus Family - Bien Hoa - Family/Maternity)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 86)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (86, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=1200&q=80', 'unsplash/p86_1', N'Mẹ và bé trong khu vườn hoa cúc họa mi ngập tràn tình yêu thương.', 'LIFESTYLE', 1, 1, DATEADD(hour, -11, SYSUTCDATETIME()));
END;

-- 16. Profile 87 (Analog Avenue - HCM - Fashion Film)
IF NOT EXISTS (SELECT 1 FROM dbo.portfolio_images WHERE photographer_profile_id = 87)
BEGIN
    INSERT INTO dbo.portfolio_images (photographer_profile_id, image_url, public_id, caption, category, is_cover, display_order, created_at)
    VALUES 
    (87, 'https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=1200&q=80', 'unsplash/p87_1', N'Nhiếp ảnh thời trang phong cách Retro 90s chụp bằng máy ảnh medium format Hasselblad.', 'FASHION', 1, 1, DATEADD(hour, -3, SYSUTCDATETIME()));
END;

PRINT 'Portfolio sample seed applied successfully!';
