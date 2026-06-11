USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'CarRentalDB does not exist. Run sql/database-schema.sql first.', 16, 1);
    RETURN;
END;
GO

USE CarRentalDB;
GO

IF OBJECT_ID(N'dbo.Users', N'U') IS NULL OR OBJECT_ID(N'dbo.Cars', N'U') IS NULL
BEGIN
    RAISERROR(N'Required tables do not exist. Run sql/database-schema.sql first.', 16, 1);
    RETURN;
END;
GO

-- Roles
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleName = N'CUSTOMER')
    INSERT INTO dbo.Roles (RoleName, Description) VALUES (N'CUSTOMER', N'Customer account');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleName = N'STAFF')
    INSERT INTO dbo.Roles (RoleName, Description) VALUES (N'STAFF', N'Nhan vien van hanh hop dong');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleName = N'MANAGER')
    INSERT INTO dbo.Roles (RoleName, Description) VALUES (N'MANAGER', N'Quan ly doi xe va tai xe');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleName = N'DRIVER')
    INSERT INTO dbo.Roles (RoleName, Description) VALUES (N'DRIVER', N'Tai xe');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleName = N'ADMIN')
    INSERT INTO dbo.Roles (RoleName, Description) VALUES (N'ADMIN', N'Quan tri he thong');
GO

-- Car types
IF NOT EXISTS (SELECT 1 FROM dbo.Car_Types WHERE SeatCount = 4)
    INSERT INTO dbo.Car_Types (TypeName, SeatCount, Description)
    VALUES (N'4 seats', 4, N'Xe 4 cho');
IF NOT EXISTS (SELECT 1 FROM dbo.Car_Types WHERE SeatCount = 5)
    INSERT INTO dbo.Car_Types (TypeName, SeatCount, Description)
    VALUES (N'5 seats', 5, N'Xe 5 cho');
IF NOT EXISTS (SELECT 1 FROM dbo.Car_Types WHERE SeatCount = 7)
    INSERT INTO dbo.Car_Types (TypeName, SeatCount, Description)
    VALUES (N'7 seats', 7, N'Xe 7 cho');
GO

-- Demo users. Passwords are plaintext because the current LoginServlet compares directly with PasswordHash.
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'admin')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'admin', N'admin@carrental.vn', N'admin123', N'System Admin', N'0900000001', N'Ha Noi', N'ADMIN-DEMO');

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'staff01')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'staff01', N'staff01@carrental.vn', N'staff123', N'Nguyen Van Staff', N'0900000002', N'Ha Noi', N'STAFF-DEMO-01');

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'manager01')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'manager01', N'manager01@carrental.vn', N'manager123', N'Tran Thi Manager', N'0900000003', N'TP HCM', N'MANAGER-DEMO-01');

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'driver01')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'driver01', N'driver01@carrental.vn', N'driver123', N'Le Van Tai Xe', N'0900000004', N'Da Nang', N'DRIVER-DEMO-01');

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'driver02')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'driver02', N'driver02@carrental.vn', N'driver123', N'Pham Minh Tai', N'0900000005', N'Ha Noi', N'DRIVER-DEMO-02');

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'customer01')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'customer01', N'customer01@gmail.com', N'cust123', N'Nguyen Khach Hang', N'0912345678', N'Quan 1, TP HCM', N'CUSTOMER-DEMO-01');

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'customer02')
    INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber)
    VALUES (N'customer02', N'customer02@gmail.com', N'cust123', N'Tran Van Khach', N'0912345679', N'Quan 7, TP HCM', N'CUSTOMER-DEMO-02');
GO

-- Role assignments
INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM dbo.Users u
JOIN dbo.Roles r ON r.RoleName = N'ADMIN'
WHERE u.Username = N'admin'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.User_Roles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID
  );

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM dbo.Users u
JOIN dbo.Roles r ON r.RoleName = N'STAFF'
WHERE u.Username = N'staff01'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.User_Roles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID
  );

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM dbo.Users u
JOIN dbo.Roles r ON r.RoleName IN (N'MANAGER', N'STAFF')
WHERE u.Username = N'manager01'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.User_Roles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID
  );

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM dbo.Users u
JOIN dbo.Roles r ON r.RoleName = N'DRIVER'
WHERE u.Username IN (N'driver01', N'driver02')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.User_Roles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID
  );

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM dbo.Users u
JOIN dbo.Roles r ON r.RoleName = N'CUSTOMER'
WHERE u.Username IN (N'customer01', N'customer02')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.User_Roles ur WHERE ur.UserID = u.UserID AND ur.RoleID = r.RoleID
  );
GO

-- Employee profiles
IF NOT EXISTS (SELECT 1 FROM dbo.Employees e JOIN dbo.Users u ON u.UserID = e.UserID WHERE u.Username = N'admin')
    INSERT INTO dbo.Employees (UserID, EmployeeCode, JobTitle)
    SELECT UserID, N'EMP-ADMIN', N'System Administrator' FROM dbo.Users WHERE Username = N'admin';

IF NOT EXISTS (SELECT 1 FROM dbo.Employees e JOIN dbo.Users u ON u.UserID = e.UserID WHERE u.Username = N'staff01')
    INSERT INTO dbo.Employees (UserID, EmployeeCode, JobTitle)
    SELECT UserID, N'EMP-001', N'Nhan vien xu ly don' FROM dbo.Users WHERE Username = N'staff01';

IF NOT EXISTS (SELECT 1 FROM dbo.Employees e JOIN dbo.Users u ON u.UserID = e.UserID WHERE u.Username = N'manager01')
    INSERT INTO dbo.Employees (UserID, EmployeeCode, JobTitle)
    SELECT UserID, N'EMP-002', N'Quan ly doi xe' FROM dbo.Users WHERE Username = N'manager01';
GO

-- Driver profiles
IF NOT EXISTS (SELECT 1 FROM dbo.Drivers d JOIN dbo.Users u ON u.UserID = d.UserID WHERE u.Username = N'driver01')
    INSERT INTO dbo.Drivers (UserID, LicenseNumber, LicenseClass, LicenseExpiryDate, BaseDailyFee)
    SELECT UserID, N'DL-2024-001', N'B2', '2030-12-31', 300000 FROM dbo.Users WHERE Username = N'driver01';

IF NOT EXISTS (SELECT 1 FROM dbo.Drivers d JOIN dbo.Users u ON u.UserID = d.UserID WHERE u.Username = N'driver02')
    INSERT INTO dbo.Drivers (UserID, LicenseNumber, LicenseClass, LicenseExpiryDate, BaseDailyFee)
    SELECT UserID, N'DL-2024-002', N'C', '2029-06-30', 350000 FROM dbo.Users WHERE Username = N'driver02';
GO

-- Customer profiles
IF NOT EXISTS (SELECT 1 FROM dbo.Customers c JOIN dbo.Users u ON u.UserID = c.UserID WHERE u.Username = N'customer01')
    INSERT INTO dbo.Customers (UserID, DriverLicenseNumber, LicenseExpiryDate)
    SELECT UserID, N'CL-2024-001', '2028-12-31' FROM dbo.Users WHERE Username = N'customer01';

IF NOT EXISTS (SELECT 1 FROM dbo.Customers c JOIN dbo.Users u ON u.UserID = c.UserID WHERE u.Username = N'customer02')
    INSERT INTO dbo.Customers (UserID, DriverLicenseNumber, LicenseExpiryDate)
    SELECT UserID, N'CL-2024-002', '2029-06-30' FROM dbo.Users WHERE Username = N'customer02';
GO

-- Cars/products
DECLARE @Type4 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 4 ORDER BY CarTypeID);
DECLARE @Type5 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 5 ORDER BY CarTypeID);
DECLARE @Type7 INT = (SELECT TOP 1 CarTypeID FROM dbo.Car_Types WHERE SeatCount = 7 ORDER BY CarTypeID);

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51A-123.45')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type4, N'51A-123.45', N'Toyota', N'Vios', 2023, N'Trang', N'AUTOMATIC', N'GASOLINE', 15000, 800000, 2000000, N'AVAILABLE', N'Toyota Vios 2023 - tiet kiem nhien lieu');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51A-234.56')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type4, N'51A-234.56', N'Hyundai', N'Accent', 2022, N'Den', N'AUTOMATIC', N'GASOLINE', 25000, 750000, 1800000, N'AVAILABLE', N'Hyundai Accent - nho gon tien loi');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51A-345.67')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type4, N'51A-345.67', N'Honda', N'City', 2023, N'Do', N'AUTOMATIC', N'GASOLINE', 10000, 850000, 2200000, N'AVAILABLE', N'Honda City RS - the thao manh me');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51B-111.11')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type5, N'51B-111.11', N'Toyota', N'Camry', 2023, N'Den', N'AUTOMATIC', N'GASOLINE', 8000, 1200000, 3000000, N'AVAILABLE', N'Toyota Camry 2.5Q - sang trong');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51B-222.22')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type5, N'51B-222.22', N'Mazda', N'CX-5', 2024, N'Xanh', N'AUTOMATIC', N'GASOLINE', 5000, 1100000, 2800000, N'AVAILABLE', N'Mazda CX-5 Premium - SUV tinh te');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51B-333.33')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type5, N'51B-333.33', N'Kia', N'K5', 2023, N'Bac', N'AUTOMATIC', N'GASOLINE', 12000, 950000, 2500000, N'AVAILABLE', N'Kia K5 - sedan hang D gia tot');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51C-444.44')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type7, N'51C-444.44', N'Toyota', N'Fortuner', 2023, N'Trang', N'AUTOMATIC', N'DIESEL', 20000, 1500000, 4000000, N'AVAILABLE', N'Toyota Fortuner - SUV manh me');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51C-555.55')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type7, N'51C-555.55', N'Ford', N'Everest', 2024, N'Xam', N'AUTOMATIC', N'DIESEL', 3000, 1600000, 4500000, N'AVAILABLE', N'Ford Everest Titanium - off-road tot');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51C-666.66')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type7, N'51C-666.66', N'Mitsubishi', N'Xpander', 2023, N'Nau', N'AUTOMATIC', N'GASOLINE', 18000, 900000, 2500000, N'AVAILABLE', N'Mitsubishi Xpander - MPV gia dinh');

IF NOT EXISTS (SELECT 1 FROM dbo.Cars WHERE LicensePlate = N'51B-999.99')
    INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
    VALUES (@Type5, N'51B-999.99', N'VinFast', N'VF 8', 2024, N'Xanh', N'AUTOMATIC', N'ELECTRIC', 2000, 1300000, 3500000, N'MAINTENANCE', N'VinFast VF 8 - xe dien VN');
GO

-- Demo car images from 4kwallpapers.com.
-- Stored as remote image URLs so a fresh clone can display images after running this seed.
UPDATE dbo.Cars
SET ImageUrl = CASE LicensePlate
    WHEN N'51A-123.45' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16338.jpg'
    WHEN N'51A-234.56' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/11004.jpeg'
    WHEN N'51A-345.67' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/7826.jpg'
    WHEN N'51B-111.11' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/24440.jpg'
    WHEN N'51B-222.22' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/24409.jpg'
    WHEN N'51B-333.33' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/16004.jpeg'
    WHEN N'51C-444.44' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/22640.jpg'
    WHEN N'51C-555.55' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/6991.jpg'
    WHEN N'51C-666.66' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/23237.jpg'
    WHEN N'51B-999.99' THEN N'https://4kwallpapers.com/images/walls/thumbs_2t/3652.jpg'
    ELSE ImageUrl
END,
UpdatedAt = SYSUTCDATETIME()
WHERE LicensePlate IN (
    N'51A-123.45', N'51A-234.56', N'51A-345.67', N'51B-111.11',
    N'51B-222.22', N'51B-333.33', N'51C-444.44', N'51C-555.55',
    N'51C-666.66', N'51B-999.99'
);
GO

SELECT N'admin' AS Username, N'admin123' AS Password, N'ADMIN' AS Roles
UNION ALL SELECT N'staff01', N'staff123', N'STAFF'
UNION ALL SELECT N'manager01', N'manager123', N'MANAGER + STAFF'
UNION ALL SELECT N'driver01', N'driver123', N'DRIVER'
UNION ALL SELECT N'driver02', N'driver123', N'DRIVER'
UNION ALL SELECT N'customer01', N'cust123', N'CUSTOMER'
UNION ALL SELECT N'customer02', N'cust123', N'CUSTOMER';

SELECT Status, COUNT(*) AS TotalCars
FROM dbo.Cars
GROUP BY Status
ORDER BY Status;
GO
