-- PhotoConnect V6: Update booking status check constraint to support all state machine statuses
DECLARE @ConstraintName NVARCHAR(200);
SELECT @ConstraintName = name FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('dbo.bookings') AND definition LIKE '%status%';
IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE dbo.bookings DROP CONSTRAINT ' + @ConstraintName);
END;
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL
BEGIN
    ALTER TABLE dbo.bookings ADD CONSTRAINT CK_bookings_status CHECK (status IN (
        'PENDING', 'ACCEPTED', 'DEPOSIT_PENDING', 'CONFIRMED', 'SHOOTING', 
        'POST_PROCESSING', 'DELIVERED', 'COMPLETED', 'REJECTED', 'CANCELLED', 
        'EXPIRED', 'DISPUTED'
    ));
END;
