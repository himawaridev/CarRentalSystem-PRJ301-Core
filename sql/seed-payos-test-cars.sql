USE CarRentalDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.Cars', N'U') IS NULL OR OBJECT_ID(N'dbo.Car_Types', N'U') IS NULL
BEGIN
    THROW 50001, 'Cars or Car_Types table does not exist. Run database-schema.sql first.', 1;
END;

DECLARE @Type4 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 4 ORDER BY CarTypeID);

IF @Type4 IS NULL
BEGIN
    INSERT INTO dbo.Car_Types (TypeName, SeatCount, Description)
    VALUES (N'4 seats', 4, N'Sedan 4 seats');
    SET @Type4 = SCOPE_IDENTITY();
END;

BEGIN TRANSACTION;

MERGE dbo.Cars AS target
USING (VALUES
    (N'TEST-2K-01', N'PayOS', N'Test 2K', CAST(2026 AS SMALLINT), N'Trang', N'AUTOMATIC', N'GASOLINE', 100, CAST(2000 AS DECIMAL(12,2)), CAST(2000 AS DECIMAL(12,2)), N'AVAILABLE', N'Xe test payOS gia thue 2.000 VND/ngay'),
    (N'TEST-2K-02', N'PayOS', N'Test 2K', CAST(2026 AS SMALLINT), N'Den', N'AUTOMATIC', N'GASOLINE', 120, CAST(2000 AS DECIMAL(12,2)), CAST(2000 AS DECIMAL(12,2)), N'AVAILABLE', N'Xe test payOS gia thue 2.000 VND/ngay'),
    (N'TEST-2K-03', N'PayOS', N'Test 2K', CAST(2026 AS SMALLINT), N'Bac', N'AUTOMATIC', N'GASOLINE', 150, CAST(2000 AS DECIMAL(12,2)), CAST(2000 AS DECIMAL(12,2)), N'AVAILABLE', N'Xe test payOS gia thue 2.000 VND/ngay'),
    (N'TEST-2K-04', N'PayOS', N'Test 2K', CAST(2026 AS SMALLINT), N'Xanh', N'AUTOMATIC', N'HYBRID', 180, CAST(2000 AS DECIMAL(12,2)), CAST(2000 AS DECIMAL(12,2)), N'AVAILABLE', N'Xe test payOS gia thue 2.000 VND/ngay'),
    (N'TEST-2K-05', N'PayOS', N'Test 2K', CAST(2026 AS SMALLINT), N'Do', N'AUTOMATIC', N'GASOLINE', 200, CAST(2000 AS DECIMAL(12,2)), CAST(2000 AS DECIMAL(12,2)), N'AVAILABLE', N'Xe test payOS gia thue 2.000 VND/ngay')
) AS source (
    LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType,
    Mileage, DailyRate, DepositAmount, Status, Description
)
ON target.LicensePlate = source.LicensePlate
WHEN MATCHED THEN
    UPDATE SET
        CarTypeID = @Type4,
        Brand = source.Brand,
        Model = source.Model,
        ManufactureYear = source.ManufactureYear,
        Color = source.Color,
        Transmission = source.Transmission,
        FuelType = source.FuelType,
        Mileage = source.Mileage,
        DailyRate = source.DailyRate,
        DepositAmount = source.DepositAmount,
        Status = source.Status,
        Description = source.Description,
        UpdatedAt = SYSUTCDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color,
        Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description
    )
    VALUES (
        @Type4, source.LicensePlate, source.Brand, source.Model, source.ManufactureYear, source.Color,
        source.Transmission, source.FuelType, source.Mileage, source.DailyRate,
        source.DepositAmount, source.Status, source.Description
    );

COMMIT TRANSACTION;

-- Demo car images from 4kwallpapers.com.
UPDATE dbo.Cars
SET ImageUrl = CASE LicensePlate
    WHEN N'TEST-2K-01' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16338.jpg'
    WHEN N'TEST-2K-02' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/11004.jpeg'
    WHEN N'TEST-2K-03' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/7826.jpg'
    WHEN N'TEST-2K-04' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/24409.jpg'
    WHEN N'TEST-2K-05' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16004.jpeg'
    ELSE ImageUrl
END,
UpdatedAt = SYSUTCDATETIME()
WHERE LicensePlate LIKE N'TEST-2K-%';

SELECT LicensePlate, Brand, Model, DailyRate, DepositAmount, Status
FROM dbo.Cars
WHERE LicensePlate LIKE N'TEST-2K-%'
ORDER BY LicensePlate;
