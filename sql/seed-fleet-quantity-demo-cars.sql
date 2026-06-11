USE CarRentalDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.Cars', N'U') IS NULL OR OBJECT_ID(N'dbo.Car_Types', N'U') IS NULL
BEGIN
    THROW 50001, 'Cars or Car_Types table does not exist. Run database-schema.sql first.', 1;
END;

DECLARE @Type4 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 4 ORDER BY CarTypeID);
DECLARE @Type5 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 5 ORDER BY CarTypeID);
DECLARE @Type7 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 7 ORDER BY CarTypeID);

IF @Type4 IS NULL OR @Type5 IS NULL OR @Type7 IS NULL
BEGIN
    THROW 50002, 'Missing one or more car types: 4, 5, 7 seats.', 1;
END;

BEGIN TRANSACTION;

MERGE dbo.Cars AS target
USING (VALUES
    (@Type4, N'51A-123.46', N'Toyota', N'Vios',      CAST(2023 AS SMALLINT), N'Trang', N'AUTOMATIC', N'GASOLINE', 16200, CAST(800000 AS DECIMAL(12,2)),  CAST(2000000 AS DECIMAL(12,2)), N'AVAILABLE', N'Toyota Vios 2023 - xe 4 cho cung dong, bien so khac'),
    (@Type4, N'51A-123.47', N'Toyota', N'Vios',      CAST(2023 AS SMALLINT), N'Bac',   N'AUTOMATIC', N'GASOLINE', 17100, CAST(800000 AS DECIMAL(12,2)),  CAST(2000000 AS DECIMAL(12,2)), N'AVAILABLE', N'Toyota Vios 2023 - xe 4 cho cung dong, bien so khac'),
    (@Type4, N'51A-345.68', N'Honda',  N'City',      CAST(2023 AS SMALLINT), N'Do',    N'AUTOMATIC', N'GASOLINE', 11600, CAST(850000 AS DECIMAL(12,2)),  CAST(2200000 AS DECIMAL(12,2)), N'AVAILABLE', N'Honda City 2023 - xe 4 cho cung dong, bien so khac'),

    (@Type5, N'51B-222.23', N'Mazda',  N'CX-5',      CAST(2024 AS SMALLINT), N'Xanh',  N'AUTOMATIC', N'GASOLINE', 6400,  CAST(1100000 AS DECIMAL(12,2)), CAST(2800000 AS DECIMAL(12,2)), N'AVAILABLE', N'Mazda CX-5 2024 - xe 5 cho cung dong, bien so khac'),
    (@Type5, N'51B-222.24', N'Mazda',  N'CX-5',      CAST(2024 AS SMALLINT), N'Den',   N'AUTOMATIC', N'GASOLINE', 7100,  CAST(1100000 AS DECIMAL(12,2)), CAST(2800000 AS DECIMAL(12,2)), N'AVAILABLE', N'Mazda CX-5 2024 - xe 5 cho cung dong, bien so khac'),
    (@Type5, N'51B-333.34', N'Kia',    N'K5',        CAST(2023 AS SMALLINT), N'Bac',   N'AUTOMATIC', N'GASOLINE', 12900, CAST(950000 AS DECIMAL(12,2)),  CAST(2500000 AS DECIMAL(12,2)), N'AVAILABLE', N'Kia K5 2023 - xe 5 cho cung dong, bien so khac'),

    (@Type7, N'51C-555.56', N'Ford',   N'Everest',   CAST(2024 AS SMALLINT), N'Xam',   N'AUTOMATIC', N'DIESEL',   4200,  CAST(1600000 AS DECIMAL(12,2)), CAST(4500000 AS DECIMAL(12,2)), N'AVAILABLE', N'Ford Everest 2024 - xe 7 cho cung dong, bien so khac'),
    (@Type7, N'51C-555.57', N'Ford',   N'Everest',   CAST(2024 AS SMALLINT), N'Den',   N'AUTOMATIC', N'DIESEL',   5100,  CAST(1600000 AS DECIMAL(12,2)), CAST(4500000 AS DECIMAL(12,2)), N'AVAILABLE', N'Ford Everest 2024 - xe 7 cho cung dong, bien so khac'),
    (@Type7, N'51C-444.45', N'Toyota', N'Fortuner',  CAST(2023 AS SMALLINT), N'Trang', N'AUTOMATIC', N'DIESEL',   21100, CAST(1500000 AS DECIMAL(12,2)), CAST(4000000 AS DECIMAL(12,2)), N'AVAILABLE', N'Toyota Fortuner 2023 - xe 7 cho cung dong, bien so khac')
) AS source (
    CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType,
    Mileage, DailyRate, DepositAmount, Status, Description
)
ON target.LicensePlate = source.LicensePlate
WHEN MATCHED THEN
    UPDATE SET
        CarTypeID = source.CarTypeID,
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
        source.CarTypeID, source.LicensePlate, source.Brand, source.Model, source.ManufactureYear,
        source.Color, source.Transmission, source.FuelType, source.Mileage,
        source.DailyRate, source.DepositAmount, source.Status, source.Description
    );

COMMIT TRANSACTION;

-- Demo car images from 4kwallpapers.com.
UPDATE dbo.Cars
SET ImageUrl = CASE LicensePlate
    WHEN N'51A-123.46' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16338.jpg'
    WHEN N'51A-123.47' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16338.jpg'
    WHEN N'51A-345.68' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/7826.jpg'
    WHEN N'51B-222.23' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/24409.jpg'
    WHEN N'51B-222.24' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/24409.jpg'
    WHEN N'51B-333.34' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16004.jpeg'
    WHEN N'51C-555.56' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/6991.jpg'
    WHEN N'51C-555.57' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/6991.jpg'
    WHEN N'51C-444.45' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/22640.jpg'
    ELSE ImageUrl
END,
UpdatedAt = SYSUTCDATETIME()
WHERE LicensePlate IN (
    N'51A-123.46', N'51A-123.47', N'51A-345.68',
    N'51B-222.23', N'51B-222.24', N'51B-333.34',
    N'51C-555.56', N'51C-555.57', N'51C-444.45'
);

SELECT Brand, Model, ManufactureYear, COUNT(*) AS AvailableQuantity,
       STRING_AGG(CONVERT(NVARCHAR(MAX), LicensePlate), N', ') AS AvailableLicensePlates
FROM dbo.Cars
WHERE Status = N'AVAILABLE'
  AND (
      LicensePlate LIKE N'51A-123.%'
      OR LicensePlate LIKE N'51A-345.%'
      OR LicensePlate LIKE N'51B-222.%'
      OR LicensePlate LIKE N'51B-333.%'
      OR LicensePlate LIKE N'51C-555.%'
      OR LicensePlate LIKE N'51C-444.%'
  )
GROUP BY Brand, Model, ManufactureYear
ORDER BY Brand, Model;
