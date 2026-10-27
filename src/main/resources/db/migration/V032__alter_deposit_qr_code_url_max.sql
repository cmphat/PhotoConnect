-- PhotoConnect V6: Extend qr_code_url to NVARCHAR(MAX) to support SVG and Data URI QR codes
IF COL_LENGTH('dbo.deposits', 'qr_code_url') IS NOT NULL
BEGIN
    ALTER TABLE dbo.deposits ALTER COLUMN qr_code_url NVARCHAR(MAX) NULL;
END;

-- Update status check constraint to include PROCESSING and CANCELLED
DECLARE @ConstraintName NVARCHAR(200);
SELECT @ConstraintName = name FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('dbo.deposits') AND definition LIKE '%status%';
IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE dbo.deposits DROP CONSTRAINT ' + @ConstraintName);
END;
IF OBJECT_ID('dbo.deposits', 'U') IS NOT NULL
BEGIN
    ALTER TABLE dbo.deposits ADD CONSTRAINT CK_deposits_status CHECK (status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED', 'FORFEITED'));
END;
