-- ============================================================
-- Test Data for CarRentalDB
-- Run this AFTER database-schema.sql
-- ============================================================
USE CarRentalDB;
GO

-- ============================================================
-- 1. Users
-- ============================================================

-- Admin user (password: admin123)
INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'admin', N'admin@carrental.vn', N'admin123', N'System Admin', N'0900000001', N'Ha Noi');

-- Staff user
INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'staff01', N'staff01@carrental.vn', N'staff123', N'Nguyen Van Staff', N'0900000002', N'Ha Noi');

-- Manager user
INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'manager01', N'manager01@carrental.vn', N'manager123', N'Tran Thi Manager', N'0900000003', N'Ho Chi Minh');

-- Driver users
INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'driver01', N'driver01@carrental.vn', N'driver123', N'Le Van Tai Xe', N'0900000004', N'Da Nang');

INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'driver02', N'driver02@carrental.vn', N'driver123', N'Pham Minh Tai', N'0900000005', N'Ha Noi');

-- Customer users
INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'customer01', N'customer01@gmail.com', N'cust123', N'Nguyen Khach Hang', N'0912345678', N'Quan 1, HCM');

INSERT INTO dbo.Users (Username, Email, PasswordHash, FullName, Phone, Address)
VALUES (N'customer02', N'customer02@gmail.com', N'cust123', N'Tran Van Khach', N'0912345679', N'Quan 7, HCM');

-- ============================================================
-- 2. Assign Roles
-- ============================================================

-- Admin -> ADMIN role
INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'admin' AND r.RoleName = N'ADMIN';

-- Staff -> STAFF role
INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'staff01' AND r.RoleName = N'STAFF';

-- Manager -> MANAGER + STAFF roles
INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'manager01' AND r.RoleName = N'MANAGER';

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'manager01' AND r.RoleName = N'STAFF';

-- Driver -> DRIVER role
INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'driver01' AND r.RoleName = N'DRIVER';

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'driver02' AND r.RoleName = N'DRIVER';

-- Customer -> CUSTOMER role
INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'customer01' AND r.RoleName = N'CUSTOMER';

INSERT INTO dbo.User_Roles (UserID, RoleID)
SELECT u.UserID, r.RoleID FROM dbo.Users u, dbo.Roles r
WHERE u.Username = N'customer02' AND r.RoleName = N'CUSTOMER';

-- ============================================================
-- 3. Employees (Staff, Manager, Admin)
-- ============================================================

INSERT INTO dbo.Employees (UserID, EmployeeCode, JobTitle)
SELECT UserID, N'EMP-ADMIN', N'System Administrator' FROM dbo.Users WHERE Username = N'admin';

INSERT INTO dbo.Employees (UserID, EmployeeCode, JobTitle)
SELECT UserID, N'EMP-001', N'Nhan vien xu ly don' FROM dbo.Users WHERE Username = N'staff01';

INSERT INTO dbo.Employees (UserID, EmployeeCode, JobTitle)
SELECT UserID, N'EMP-002', N'Quan ly doi xe' FROM dbo.Users WHERE Username = N'manager01';

-- ============================================================
-- 4. Drivers
-- ============================================================

INSERT INTO dbo.Drivers (UserID, LicenseNumber, LicenseClass, LicenseExpiryDate, BaseDailyFee)
SELECT UserID, N'DL-2024-001', N'B2', '2030-12-31', 300000 FROM dbo.Users WHERE Username = N'driver01';

INSERT INTO dbo.Drivers (UserID, LicenseNumber, LicenseClass, LicenseExpiryDate, BaseDailyFee)
SELECT UserID, N'DL-2024-002', N'C', '2029-06-30', 350000 FROM dbo.Users WHERE Username = N'driver02';

-- ============================================================
-- 5. Customers
-- ============================================================

INSERT INTO dbo.Customers (UserID, DriverLicenseNumber, LicenseExpiryDate)
SELECT UserID, N'CL-2024-001', '2028-12-31' FROM dbo.Users WHERE Username = N'customer01';

INSERT INTO dbo.Customers (UserID, DriverLicenseNumber, LicenseExpiryDate)
SELECT UserID, N'CL-2024-002', '2029-06-30' FROM dbo.Users WHERE Username = N'customer02';

-- ============================================================
-- 6. Cars
-- ============================================================

-- 4-seat cars
INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (1, N'51A-123.45', N'Toyota', N'Vios', 2023, N'Trang', N'AUTOMATIC', N'GASOLINE', 15000, 800000, 2000000, N'AVAILABLE', N'Toyota Vios 2023 - tiet kiem nhien lieu');

INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (1, N'51A-234.56', N'Hyundai', N'Accent', 2022, N'Den', N'AUTOMATIC', N'GASOLINE', 25000, 750000, 1800000, N'AVAILABLE', N'Hyundai Accent - nho gon tien loi');

INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (1, N'51A-345.67', N'Honda', N'City', 2023, N'Do', N'AUTOMATIC', N'GASOLINE', 10000, 850000, 2200000, N'AVAILABLE', N'Honda City RS - the thao manh me');

-- 5-seat cars
INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (2, N'51B-111.11', N'Toyota', N'Camry', 2023, N'Den', N'AUTOMATIC', N'GASOLINE', 8000, 1200000, 3000000, N'AVAILABLE', N'Toyota Camry 2.5Q - sang trong');

INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (2, N'51B-222.22', N'Mazda', N'CX-5', 2024, N'Xanh', N'AUTOMATIC', N'GASOLINE', 5000, 1100000, 2800000, N'AVAILABLE', N'Mazda CX-5 Premium - SUV tinh te');

INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (2, N'51B-333.33', N'Kia', N'K5', 2023, N'Bac', N'AUTOMATIC', N'GASOLINE', 12000, 950000, 2500000, N'AVAILABLE', N'Kia K5 - sedan hang D gia tot');

-- 7-seat cars
INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (3, N'51C-444.44', N'Toyota', N'Fortuner', 2023, N'Trang', N'AUTOMATIC', N'DIESEL', 20000, 1500000, 4000000, N'AVAILABLE', N'Toyota Fortuner - SUV manh me');

INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (3, N'51C-555.55', N'Ford', N'Everest', 2024, N'Xam', N'AUTOMATIC', N'DIESEL', 3000, 1600000, 4500000, N'AVAILABLE', N'Ford Everest Titanium - off-road tot');

INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (3, N'51C-666.66', N'Mitsubishi', N'Xpander', 2023, N'Nau', N'AUTOMATIC', N'GASOLINE', 18000, 900000, 2500000, N'AVAILABLE', N'Mitsubishi Xpander - MPV gia dinh');

-- One car in maintenance
INSERT INTO dbo.Cars (CarTypeID, LicensePlate, Brand, Model, ManufactureYear, Color, Transmission, FuelType, Mileage, DailyRate, DepositAmount, Status, Description)
VALUES (2, N'51B-999.99', N'VinFast', N'VF 8', 2024, N'Xanh', N'AUTOMATIC', N'ELECTRIC', 2000, 1300000, 3500000, N'MAINTENANCE', N'VinFast VF 8 - xe dien VN');

PRINT N'=== Test data inserted successfully! ===';
PRINT N'';
PRINT N'Test accounts:';
PRINT N'  admin    / admin123    (ADMIN)';
PRINT N'  staff01  / staff123    (STAFF)';
PRINT N'  manager01/ manager123  (MANAGER + STAFF)';
PRINT N'  driver01 / driver123   (DRIVER)';
PRINT N'  driver02 / driver123   (DRIVER)';
PRINT N'  customer01/ cust123    (CUSTOMER)';
PRINT N'  customer02/ cust123    (CUSTOMER)';
GO
