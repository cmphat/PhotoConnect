-- V039__create_customer_posts.sql
-- Supports customer posts / photography shoot inquiries on the feed

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'customer_posts')
BEGIN
    CREATE TABLE dbo.customer_posts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        customer_id BIGINT NOT NULL,
        image_url VARCHAR(2048) NULL,
        caption NVARCHAR(1000) NOT NULL,
        category VARCHAR(50) NULL,
        budget DECIMAL(12,2) NULL,
        location NVARCHAR(150) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        CONSTRAINT FK_customer_posts_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(id) ON DELETE CASCADE
    );

    CREATE INDEX IX_customer_posts_created_at ON dbo.customer_posts (created_at DESC);
    CREATE INDEX IX_customer_posts_category ON dbo.customer_posts (category);
    CREATE INDEX IX_customer_posts_customer_id ON dbo.customer_posts (customer_id);
END
GO

-- Seed realistic customer inquiry posts for testing if user 123 exists
IF EXISTS (SELECT 1 FROM dbo.users WHERE id = 123) AND NOT EXISTS (SELECT 1 FROM dbo.customer_posts WHERE customer_id = 123)
BEGIN
    INSERT INTO dbo.customer_posts (customer_id, image_url, caption, category, budget, location, created_at)
    VALUES 
    (123, 
     'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=1200&q=80', 
     N'Cần tìm photographer chụp bộ ảnh cưới phóng sự tại Đà Lạt vào giữa tháng tới. Mình thích tone màu vintage ấm áp, tự nhiên.', 
     'WEDDING', 
     5000000, 
     N'Đà Lạt, Lâm Đồng', 
     DATEADD(HOUR, -6, GETDATE())),
    (123, 
     'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80', 
     N'Tìm bạn photographer chụp lookbook thời trang dạo phố concept Y2K tại phố đi bộ Nguyễn Huệ, TP.HCM. Cần chụp khoảng 2-3 tiếng.', 
     'FASHION', 
     2000000, 
     N'TP. Hồ Chí Minh', 
     DATEADD(DAY, -1, GETDATE())),
    (123, 
     'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1200&q=80', 
     N'Mình muốn chụp bộ ảnh chân dung cá nhân kỷ niệm sinh nhật tại studio có setup ánh sáng nghệ thuật. Photographer nào nhận slot cuối tuần này nhắn mình nhé!', 
     'PORTRAIT', 
     1500000, 
     N'Hà Nội', 
     DATEADD(DAY, -2, GETDATE()));
END
GO
