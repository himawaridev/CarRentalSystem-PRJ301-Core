IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    CREATE DATABASE CarRentalDB;
END
GO

USE CarRentalDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

CREATE TABLE dbo.Roles (
    RoleID INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(30) NOT NULL UNIQUE,
    Description NVARCHAR(255) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_Roles_RoleName CHECK (
        RoleName IN (N'CUSTOMER', N'STAFF', N'MANAGER', N'DRIVER', N'ADMIN')
    )
);

CREATE TABLE dbo.Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    FullName NVARCHAR(120) NOT NULL,
    Phone NVARCHAR(30) NULL,
    Address NVARCHAR(255) NULL,
    IdentityNumber NVARCHAR(30) NULL,
    BankCode NVARCHAR(30) NULL,
    BankName NVARCHAR(120) NULL,
    BankAccountNumber NVARCHAR(30) NULL,
    BankAccountHolder NVARCHAR(120) NULL,
    DateOfBirth DATE NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT CK_Users_Status CHECK (
        Status IN (N'ACTIVE', N'LOCKED', N'DISABLED')
    )
);

CREATE UNIQUE INDEX UX_Users_Phone_NotNull
ON dbo.Users(Phone)
WHERE Phone IS NOT NULL;

CREATE INDEX IX_Users_BankAccountNumber_NotNull
ON dbo.Users(BankAccountNumber)
WHERE BankAccountNumber IS NOT NULL;

CREATE TABLE dbo.User_Roles (
    UserID INT NOT NULL,
    RoleID INT NOT NULL,
    AssignedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    AssignedByUserID INT NULL,
    PRIMARY KEY (UserID, RoleID),
    CONSTRAINT FK_UserRoles_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_UserRoles_Roles FOREIGN KEY (RoleID)
        REFERENCES dbo.Roles(RoleID),
    CONSTRAINT FK_UserRoles_AssignedBy FOREIGN KEY (AssignedByUserID)
        REFERENCES dbo.Users(UserID)
);

CREATE TABLE dbo.Customers (
    CustomerID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL UNIQUE,
    DriverLicenseNumber NVARCHAR(50) NULL,
    LicenseExpiryDate DATE NULL,
    Notes NVARCHAR(500) NULL,
    CONSTRAINT FK_Customers_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID)
);

CREATE TABLE dbo.Employees (
    EmployeeID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL UNIQUE,
    EmployeeCode NVARCHAR(30) NOT NULL UNIQUE,
    JobTitle NVARCHAR(80) NULL,
    HireDate DATE NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    CONSTRAINT FK_Employees_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Employees_Status CHECK (
        Status IN (N'ACTIVE', N'INACTIVE')
    )
);

CREATE TABLE dbo.Drivers (
    DriverID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL UNIQUE,
    LicenseNumber NVARCHAR(50) NOT NULL UNIQUE,
    LicenseClass NVARCHAR(20) NOT NULL,
    LicenseExpiryDate DATE NOT NULL,
    BaseDailyFee DECIMAL(12,2) NOT NULL DEFAULT 0,
    EmploymentStatus NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Drivers_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Drivers_BaseDailyFee CHECK (BaseDailyFee >= 0),
    CONSTRAINT CK_Drivers_Status CHECK (
        EmploymentStatus IN (N'ACTIVE', N'ON_LEAVE', N'INACTIVE')
    )
);

CREATE TABLE dbo.System_Settings (
    SettingKey NVARCHAR(100) PRIMARY KEY,
    SettingValue NVARCHAR(500) NOT NULL,
    DataType NVARCHAR(30) NOT NULL DEFAULT N'STRING',
    Description NVARCHAR(255) NULL,
    UpdatedByUserID INT NULL,
    UpdatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_SystemSettings_Users FOREIGN KEY (UpdatedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_SystemSettings_DataType CHECK (
        DataType IN (N'STRING', N'NUMBER', N'BOOLEAN', N'JSON')
    )
);

CREATE TABLE dbo.Car_Types (
    CarTypeID INT IDENTITY(1,1) PRIMARY KEY,
    TypeName NVARCHAR(50) NOT NULL UNIQUE,
    SeatCount TINYINT NOT NULL,
    Description NVARCHAR(255) NULL,
    CONSTRAINT CK_CarTypes_SeatCount CHECK (SeatCount IN (4, 5, 7))
);

CREATE TABLE dbo.Cars (
    CarID INT IDENTITY(1,1) PRIMARY KEY,
    CarTypeID INT NOT NULL,
    LicensePlate NVARCHAR(20) NOT NULL UNIQUE,
    Brand NVARCHAR(60) NOT NULL,
    Model NVARCHAR(60) NOT NULL,
    ManufactureYear SMALLINT NULL,
    Color NVARCHAR(40) NULL,
    Transmission NVARCHAR(20) NULL,
    FuelType NVARCHAR(20) NULL,
    Mileage INT NOT NULL DEFAULT 0,
    DailyRate DECIMAL(12,2) NOT NULL,
    DepositAmount DECIMAL(12,2) NOT NULL DEFAULT 0,
    Status NVARCHAR(20) NOT NULL DEFAULT N'AVAILABLE',
    ImageUrl NVARCHAR(500) NULL,
    Description NVARCHAR(500) NULL,
    CreatedByUserID INT NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT FK_Cars_CarTypes FOREIGN KEY (CarTypeID)
        REFERENCES dbo.Car_Types(CarTypeID),
    CONSTRAINT FK_Cars_CreatedBy FOREIGN KEY (CreatedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Cars_Mileage CHECK (Mileage >= 0),
    CONSTRAINT CK_Cars_DailyRate CHECK (DailyRate >= 0),
    CONSTRAINT CK_Cars_DepositAmount CHECK (DepositAmount >= 0),
    CONSTRAINT CK_Cars_ManufactureYear CHECK (
        ManufactureYear IS NULL OR ManufactureYear BETWEEN 1980 AND 2100
    ),
    CONSTRAINT CK_Cars_Transmission CHECK (
        Transmission IS NULL OR Transmission IN (N'MANUAL', N'AUTOMATIC')
    ),
    CONSTRAINT CK_Cars_FuelType CHECK (
        FuelType IS NULL OR FuelType IN (N'GASOLINE', N'DIESEL', N'HYBRID', N'ELECTRIC')
    ),
    CONSTRAINT CK_Cars_Status CHECK (
        Status IN (N'AVAILABLE', N'MAINTENANCE', N'INACTIVE', N'RETIRED')
    )
);

CREATE TABLE dbo.Car_Maintenance (
    MaintenanceID BIGINT IDENTITY(1,1) PRIMARY KEY,
    CarID INT NOT NULL,
    StartAt DATETIME2(0) NOT NULL,
    EndAt DATETIME2(0) NOT NULL,
    Reason NVARCHAR(255) NOT NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT N'SCHEDULED',
    CreatedByUserID INT NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_CarMaintenance_Cars FOREIGN KEY (CarID)
        REFERENCES dbo.Cars(CarID),
    CONSTRAINT FK_CarMaintenance_Users FOREIGN KEY (CreatedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_CarMaintenance_DateRange CHECK (EndAt > StartAt),
    CONSTRAINT CK_CarMaintenance_Status CHECK (
        Status IN (N'SCHEDULED', N'IN_PROGRESS', N'COMPLETED', N'CANCELLED')
    )
);

CREATE TABLE dbo.Contracts (
    ContractID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractCode NVARCHAR(30) NOT NULL UNIQUE,
    CustomerID INT NOT NULL,
    PickupAt DATETIME2(0) NOT NULL,
    ReturnAt DATETIME2(0) NOT NULL,
    PickupLocation NVARCHAR(255) NOT NULL,
    ReturnLocation NVARCHAR(255) NOT NULL,
    Status NVARCHAR(40) NOT NULL DEFAULT N'PENDING_PAYMENT',
    ReviewedByUserID INT NULL,
    ReviewedAt DATETIME2(0) NULL,
    ReviewNote NVARCHAR(500) NULL,
    RejectionReason NVARCHAR(500) NULL,
    DepositAmountDue DECIMAL(12,2) NOT NULL DEFAULT 0,
    FinalAmountDue DECIMAL(12,2) NOT NULL DEFAULT 0,
    DepositPaidAt DATETIME2(0) NULL,
    FinalPaidAt DATETIME2(0) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT FK_Contracts_Customers FOREIGN KEY (CustomerID)
        REFERENCES dbo.Customers(CustomerID),
    CONSTRAINT FK_Contracts_ReviewedBy FOREIGN KEY (ReviewedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Contracts_DateRange CHECK (ReturnAt > PickupAt),
    CONSTRAINT CK_Contracts_Amounts CHECK (
        DepositAmountDue >= 0 AND FinalAmountDue >= 0
    ),
    CONSTRAINT CK_Contracts_Status CHECK (
        Status IN (
            N'PENDING_PAYMENT',
            N'PAYMENT_EXPIRED',
            N'RESERVED',
            N'CONFIRMED',
            N'CANCELLED',
            N'CAR_PICKED_UP',
            N'CAR_RETURNED',
            N'SETTLEMENT_PENDING',
            N'COMPLETED'
        )
    )
);

CREATE TABLE dbo.Contract_Details (
    ContractDetailID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    CarID INT NOT NULL,
    RequiresDriver BIT NOT NULL DEFAULT 0,
    RentalDailyRate DECIMAL(12,2) NOT NULL,
    DriverDailyRate DECIMAL(12,2) NOT NULL DEFAULT 0,
    EstimatedDays DECIMAL(8,2) NOT NULL,
    RentalAmount DECIMAL(12,2) NOT NULL,
    DriverAmount DECIMAL(12,2) NOT NULL DEFAULT 0,
    LineTotal AS (RentalAmount + DriverAmount) PERSISTED,
    DetailStatus NVARCHAR(30) NOT NULL DEFAULT N'BOOKED',
    PickupOdometer INT NULL,
    ReturnOdometer INT NULL,
    Notes NVARCHAR(500) NULL,
    CONSTRAINT FK_ContractDetails_Contracts FOREIGN KEY (ContractID)
        REFERENCES dbo.Contracts(ContractID),
    CONSTRAINT FK_ContractDetails_Cars FOREIGN KEY (CarID)
        REFERENCES dbo.Cars(CarID),
    CONSTRAINT UQ_ContractDetails_Contract_Car UNIQUE (ContractID, CarID),
    CONSTRAINT CK_ContractDetails_Rates CHECK (
        RentalDailyRate >= 0 AND DriverDailyRate >= 0
    ),
    CONSTRAINT CK_ContractDetails_Amounts CHECK (
        EstimatedDays > 0 AND RentalAmount >= 0 AND DriverAmount >= 0
    ),
    CONSTRAINT CK_ContractDetails_Odometer CHECK (
        PickupOdometer IS NULL OR ReturnOdometer IS NULL OR ReturnOdometer >= PickupOdometer
    ),
    CONSTRAINT CK_ContractDetails_Status CHECK (
        DetailStatus IN (N'BOOKED', N'PICKED_UP', N'RETURNED', N'CANCELLED')
    )
);

CREATE TABLE dbo.Driver_Assignments (
    AssignmentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractDetailID BIGINT NOT NULL,
    DriverID INT NOT NULL,
    AssignedByUserID INT NULL,
    AssignedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    AssignmentStatus NVARCHAR(30) NOT NULL DEFAULT N'ASSIGNED',
    HandoverReceivedAt DATETIME2(0) NULL,
    TripCompletedAt DATETIME2(0) NULL,
    DriverNote NVARCHAR(500) NULL,
    CONSTRAINT FK_DriverAssignments_ContractDetails FOREIGN KEY (ContractDetailID)
        REFERENCES dbo.Contract_Details(ContractDetailID),
    CONSTRAINT FK_DriverAssignments_Drivers FOREIGN KEY (DriverID)
        REFERENCES dbo.Drivers(DriverID),
    CONSTRAINT FK_DriverAssignments_AssignedBy FOREIGN KEY (AssignedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_DriverAssignments_Status CHECK (
        AssignmentStatus IN (
            N'ASSIGNED',
            N'HANDOVER_RECEIVED',
            N'TRIP_IN_PROGRESS',
            N'TRIP_COMPLETED',
            N'CANCELLED'
        )
    )
);

CREATE UNIQUE INDEX UX_DriverAssignments_ActiveDetail
ON dbo.Driver_Assignments(ContractDetailID)
WHERE AssignmentStatus <> N'CANCELLED';

CREATE TABLE dbo.Car_Handovers (
    HandoverID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractDetailID BIGINT NOT NULL,
    HandoverType NVARCHAR(20) NOT NULL,
    HandoverAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    Odometer INT NULL,
    FuelLevel NVARCHAR(30) NULL,
    CarConditionNote NVARCHAR(500) NULL,
    StaffUserID INT NULL,
    DriverID INT NULL,
    CustomerConfirmed BIT NOT NULL DEFAULT 0,
    DriverConfirmed BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_CarHandovers_ContractDetails FOREIGN KEY (ContractDetailID)
        REFERENCES dbo.Contract_Details(ContractDetailID),
    CONSTRAINT FK_CarHandovers_Staff FOREIGN KEY (StaffUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_CarHandovers_Drivers FOREIGN KEY (DriverID)
        REFERENCES dbo.Drivers(DriverID),
    CONSTRAINT UQ_CarHandovers_Detail_Type UNIQUE (ContractDetailID, HandoverType),
    CONSTRAINT CK_CarHandovers_Type CHECK (
        HandoverType IN (N'PICKUP', N'RETURN')
    ),
    CONSTRAINT CK_CarHandovers_Odometer CHECK (
        Odometer IS NULL OR Odometer >= 0
    )
);

CREATE TABLE dbo.Payment_Transactions (
    PaymentTransactionID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    Provider NVARCHAR(40) NOT NULL,
    ProviderTransactionRef NVARCHAR(100) NOT NULL UNIQUE,
    ProviderOrderCode BIGINT NULL,
    ProviderPaymentRef NVARCHAR(100) NULL,
    Amount DECIMAL(12,2) NOT NULL,
    Status NVARCHAR(30) NOT NULL DEFAULT N'PENDING',
    QrPayload NVARCHAR(1000) NULL,
    ProviderCheckoutUrl NVARCHAR(500) NULL,
    ProviderQrCode NVARCHAR(MAX) NULL,
    ExpiredAt DATETIME2(0) NULL,
    PaidAt DATETIME2(0) NULL,
    Metadata NVARCHAR(MAX) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT FK_PaymentTransactions_Contracts FOREIGN KEY (ContractID)
        REFERENCES dbo.Contracts(ContractID),
    CONSTRAINT CK_PaymentTransactions_Amount CHECK (Amount > 0),
    CONSTRAINT CK_PaymentTransactions_Status CHECK (
        Status IN (
            N'PENDING',
            N'PAID',
            N'FAILED',
            N'EXPIRED',
            N'REFUND_PENDING',
            N'REFUNDED',
            N'PARTIALLY_REFUNDED'
        )
    )
);

CREATE TABLE dbo.Payment_Webhook_Events (
    WebhookEventID BIGINT IDENTITY(1,1) PRIMARY KEY,
    Provider NVARCHAR(40) NOT NULL,
    EventRef NVARCHAR(150) NOT NULL,
    ProviderTransactionRef NVARCHAR(100) NULL,
    Payload NVARCHAR(MAX) NULL,
    Signature NVARCHAR(200) NULL,
    ProcessingStatus NVARCHAR(30) NOT NULL DEFAULT N'RECEIVED',
    ErrorMessage NVARCHAR(500) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    ProcessedAt DATETIME2(0) NULL,
    CONSTRAINT UQ_PaymentWebhookEvents_Provider_EventRef UNIQUE (Provider, EventRef),
    CONSTRAINT CK_PaymentWebhookEvents_Status CHECK (
        ProcessingStatus IN (N'RECEIVED', N'PROCESSED', N'DUPLICATE', N'FAILED')
    )
);

CREATE TABLE dbo.Payments (
    PaymentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    PaymentTransactionID BIGINT NULL,
    SourcePaymentID BIGINT NULL,
    PaymentType NVARCHAR(30) NOT NULL,
    Amount DECIMAL(12,2) NOT NULL,
    PaymentMethod NVARCHAR(30) NOT NULL,
    PaymentStatus NVARCHAR(30) NOT NULL DEFAULT N'PENDING',
    PaidAt DATETIME2(0) NULL,
    ReceivedByUserID INT NULL,
    TransactionRef NVARCHAR(100) NULL,
    Note NVARCHAR(500) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Payments_Contracts FOREIGN KEY (ContractID)
        REFERENCES dbo.Contracts(ContractID),
    CONSTRAINT FK_Payments_PaymentTransactions FOREIGN KEY (PaymentTransactionID)
        REFERENCES dbo.Payment_Transactions(PaymentTransactionID),
    CONSTRAINT FK_Payments_SourcePayment FOREIGN KEY (SourcePaymentID)
        REFERENCES dbo.Payments(PaymentID),
    CONSTRAINT FK_Payments_ReceivedBy FOREIGN KEY (ReceivedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Payments_Amount CHECK (Amount > 0),
    CONSTRAINT CK_Payments_Type CHECK (
        PaymentType IN (
            N'DEPOSIT',
            N'RENTAL_PREPAID',
            N'DRIVER_FEE_PREPAID',
            N'RENTAL_BALANCE',
            N'EXTRA_CHARGE',
            N'REFUND'
        )
    ),
    CONSTRAINT CK_Payments_Method CHECK (
        PaymentMethod IN (N'CASH', N'BANK_TRANSFER', N'CARD', N'E_WALLET', N'OTHER')
    ),
    CONSTRAINT CK_Payments_Status CHECK (
        PaymentStatus IN (
            N'PENDING',
            N'PAID',
            N'FAILED',
            N'EXPIRED',
            N'REFUND_PENDING',
            N'REFUNDED',
            N'PARTIALLY_REFUNDED'
        )
    )
);

CREATE TABLE dbo.Refunds (
    RefundID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    SourcePaymentID BIGINT NOT NULL,
    DepositAmount DECIMAL(12,2) NOT NULL,
    DeductionAmount DECIMAL(12,2) NOT NULL DEFAULT 0,
    RefundAmount DECIMAL(12,2) NOT NULL,
    Reason NVARCHAR(500) NULL,
    RefundMethod NVARCHAR(30) NOT NULL DEFAULT N'GATEWAY_REFUND',
    ProofOfRefund NVARCHAR(1000) NULL,
    Status NVARCHAR(30) NOT NULL DEFAULT N'REFUND_PENDING',
    ApprovedByUserID INT NULL,
    CompletedByUserID INT NULL,
    ProviderRefundRef NVARCHAR(100) NULL,
    CompletedAt DATETIME2(0) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT FK_Refunds_Contracts FOREIGN KEY (ContractID)
        REFERENCES dbo.Contracts(ContractID),
    CONSTRAINT FK_Refunds_SourcePayment FOREIGN KEY (SourcePaymentID)
        REFERENCES dbo.Payments(PaymentID),
    CONSTRAINT FK_Refunds_ApprovedBy FOREIGN KEY (ApprovedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_Refunds_CompletedBy FOREIGN KEY (CompletedByUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_Refunds_Amounts CHECK (
        DepositAmount >= 0 AND DeductionAmount >= 0 AND RefundAmount >= 0
    ),
    CONSTRAINT CK_Refunds_Method CHECK (
        RefundMethod IN (
            N'GATEWAY_REFUND',
            N'CASH_AT_COUNTER',
            N'MANUAL_BANK_TRANSFER',
            N'WALLET_CREDIT'
        )
    ),
    CONSTRAINT CK_Refunds_Status CHECK (
        Status IN (N'REFUND_PENDING', N'REFUNDED', N'FAILED')
    )
);

CREATE TABLE dbo.Contract_Status_History (
    HistoryID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    OldStatus NVARCHAR(40) NULL,
    NewStatus NVARCHAR(40) NOT NULL,
    ChangedByUserID INT NULL,
    ChangedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    Note NVARCHAR(500) NULL,
    CONSTRAINT FK_ContractStatusHistory_Contracts FOREIGN KEY (ContractID)
        REFERENCES dbo.Contracts(ContractID),
    CONSTRAINT FK_ContractStatusHistory_Users FOREIGN KEY (ChangedByUserID)
        REFERENCES dbo.Users(UserID)
);

CREATE INDEX IX_Cars_Type_Status
ON dbo.Cars(CarTypeID, Status);

CREATE INDEX IX_Contracts_Status_Dates
ON dbo.Contracts(Status, PickupAt, ReturnAt);

CREATE INDEX IX_ContractDetails_Car
ON dbo.Contract_Details(CarID, ContractID);

CREATE INDEX IX_DriverAssignments_Driver_Status
ON dbo.Driver_Assignments(DriverID, AssignmentStatus);

CREATE INDEX IX_Payments_Contract
ON dbo.Payments(ContractID);

CREATE INDEX IX_PaymentTransactions_Contract_Status
ON dbo.Payment_Transactions(ContractID, Status);

CREATE UNIQUE INDEX UX_PaymentTransactions_ProviderOrderCode
ON dbo.Payment_Transactions(ProviderOrderCode)
WHERE ProviderOrderCode IS NOT NULL;

CREATE INDEX IX_Refunds_Contract
ON dbo.Refunds(ContractID);

CREATE INDEX IX_CarMaintenance_Car_Dates
ON dbo.Car_Maintenance(CarID, StartAt, EndAt, Status);
GO

CREATE TRIGGER dbo.TR_ContractDetails_PreventCarOverlap
ON dbo.Contract_Details
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.Contracts c1 ON i.ContractID = c1.ContractID
        JOIN dbo.Contract_Details cd2
            ON cd2.CarID = i.CarID
            AND cd2.ContractDetailID <> i.ContractDetailID
        JOIN dbo.Contracts c2 ON cd2.ContractID = c2.ContractID
        WHERE i.DetailStatus <> N'CANCELLED'
          AND cd2.DetailStatus <> N'CANCELLED'
          AND c1.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c2.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c1.PickupAt < c2.ReturnAt
          AND c1.ReturnAt > c2.PickupAt
    )
    BEGIN
        RAISERROR(N'This car is already reserved for an overlapping rental period.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

CREATE TRIGGER dbo.TR_Contracts_PreventScheduleConflicts
ON dbo.Contracts
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted c1
        JOIN dbo.Contract_Details cd1
            ON c1.ContractID = cd1.ContractID
            AND cd1.DetailStatus <> N'CANCELLED'
        JOIN dbo.Contract_Details cd2
            ON cd1.CarID = cd2.CarID
            AND cd1.ContractDetailID <> cd2.ContractDetailID
            AND cd2.DetailStatus <> N'CANCELLED'
        JOIN dbo.Contracts c2 ON cd2.ContractID = c2.ContractID
        WHERE c1.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c2.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c1.PickupAt < c2.ReturnAt
          AND c1.ReturnAt > c2.PickupAt
    )
    BEGIN
        RAISERROR(N'Contract dates conflict with an existing car reservation.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    IF EXISTS (
        SELECT 1
        FROM inserted c1
        JOIN dbo.Contract_Details cd1 ON c1.ContractID = cd1.ContractID
        JOIN dbo.Driver_Assignments da1
            ON cd1.ContractDetailID = da1.ContractDetailID
            AND da1.AssignmentStatus <> N'CANCELLED'
        JOIN dbo.Driver_Assignments da2
            ON da1.DriverID = da2.DriverID
            AND da1.AssignmentID <> da2.AssignmentID
            AND da2.AssignmentStatus <> N'CANCELLED'
        JOIN dbo.Contract_Details cd2 ON da2.ContractDetailID = cd2.ContractDetailID
        JOIN dbo.Contracts c2 ON cd2.ContractID = c2.ContractID
        WHERE c1.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c2.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c1.PickupAt < c2.ReturnAt
          AND c1.ReturnAt > c2.PickupAt
    )
    BEGIN
        RAISERROR(N'Contract dates conflict with an existing driver assignment.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

CREATE TRIGGER dbo.TR_DriverAssignments_Validate
ON dbo.Driver_Assignments
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.Contract_Details cd ON i.ContractDetailID = cd.ContractDetailID
        WHERE cd.RequiresDriver = 0
          AND i.AssignmentStatus <> N'CANCELLED'
    )
    BEGIN
        RAISERROR(N'Cannot assign a driver to a contract detail that does not require a driver.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.Contract_Details cd1 ON i.ContractDetailID = cd1.ContractDetailID
        JOIN dbo.Contracts c1 ON cd1.ContractID = c1.ContractID
        JOIN dbo.Driver_Assignments da2
            ON i.DriverID = da2.DriverID
            AND i.AssignmentID <> da2.AssignmentID
            AND da2.AssignmentStatus <> N'CANCELLED'
        JOIN dbo.Contract_Details cd2 ON da2.ContractDetailID = cd2.ContractDetailID
        JOIN dbo.Contracts c2 ON cd2.ContractID = c2.ContractID
        WHERE i.AssignmentStatus <> N'CANCELLED'
          AND c1.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c2.Status IN (N'RESERVED', N'CONFIRMED', N'CAR_PICKED_UP')
          AND c1.PickupAt < c2.ReturnAt
          AND c1.ReturnAt > c2.PickupAt
    )
    BEGIN
        RAISERROR(N'This driver is already assigned during an overlapping rental period.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

INSERT INTO dbo.Roles (RoleName, Description)
VALUES
(N'CUSTOMER', N'Customer account'),
(N'STAFF', N'Nhân viên: reviews bookings and updates contracts'),
(N'MANAGER', N'Quản lý: manages fleet and driver assignments'),
(N'DRIVER', N'Tài xế: views schedule and updates trips'),
(N'ADMIN', N'Full system administrator');

INSERT INTO dbo.Car_Types (TypeName, SeatCount, Description)
VALUES
(N'4 seats', 4, N'Small car'),
(N'5 seats', 5, N'Sedan or compact SUV'),
(N'7 seats', 7, N'Large SUV or MPV');

INSERT INTO dbo.System_Settings (SettingKey, SettingValue, DataType, Description)
VALUES
(N'DEPOSIT_REQUIRED_BEFORE_RESERVATION', N'true', N'BOOLEAN', N'Deposit must be paid before the car is reserved'),
(N'DEFAULT_CURRENCY', N'VND', N'STRING', N'Default display currency');
GO
