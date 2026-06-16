/*
    CarRentalSystem fresh database setup.
    Use this file for a new local SQL Server database.
    Do not run this on an existing production database; use sql/upgrade-existing-database.sql instead.
*/


/* ============================================================
   1. Schema, constraints, indexes, triggers, base configuration
   ============================================================ */

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
    EmailVerified BIT NOT NULL DEFAULT 1,
    EmailVerifiedAt DATETIME2(0) NULL,
    AuthProvider NVARCHAR(30) NULL,
    AuthProviderSubject NVARCHAR(120) NULL,
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

CREATE UNIQUE INDEX UX_Users_AuthProviderSubject_NotNull
ON dbo.Users(AuthProvider, AuthProviderSubject)
WHERE AuthProvider IS NOT NULL AND AuthProviderSubject IS NOT NULL;

CREATE TABLE dbo.Pending_Registrations (
    PendingRegistrationID BIGINT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL,
    Email NVARCHAR(255) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    FullName NVARCHAR(120) NOT NULL,
    Phone NVARCHAR(30) NULL,
    Address NVARCHAR(255) NULL,
    VerificationCodeHash NVARCHAR(255) NOT NULL,
    ExpiresAt DATETIME2(0) NOT NULL,
    Attempts INT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT CK_PendingRegistrations_Attempts CHECK (Attempts >= 0)
);

CREATE UNIQUE INDEX UX_PendingRegistrations_Username
ON dbo.Pending_Registrations(Username);

CREATE UNIQUE INDEX UX_PendingRegistrations_Email
ON dbo.Pending_Registrations(Email);

CREATE TABLE dbo.Password_Reset_Codes (
    PasswordResetID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    CodeHash NVARCHAR(255) NOT NULL,
    ExpiresAt DATETIME2(0) NOT NULL,
    Attempts INT NOT NULL DEFAULT 0,
    ConsumedAt DATETIME2(0) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_PasswordResetCodes_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_PasswordResetCodes_Attempts CHECK (Attempts >= 0)
);

CREATE INDEX IX_PasswordResetCodes_User_Active
ON dbo.Password_Reset_Codes(UserID, ConsumedAt, CreatedAt DESC);

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

CREATE TABLE dbo.Support_Tickets (
    TicketID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TicketCode NVARCHAR(30) NOT NULL UNIQUE,
    UserID INT NOT NULL,
    ContractID BIGINT NULL,
    Category NVARCHAR(40) NOT NULL,
    Subject NVARCHAR(150) NOT NULL,
    Message NVARCHAR(1000) NOT NULL,
    Status NVARCHAR(30) NOT NULL DEFAULT N'OPEN',
    Priority NVARCHAR(20) NOT NULL DEFAULT N'NORMAL',
    StaffResponse NVARCHAR(1000) NULL,
    AssignedToUserID INT NULL,
    ResolvedAt DATETIME2(0) NULL,
    CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2(0) NULL,
    CONSTRAINT FK_SupportTickets_Users FOREIGN KEY (UserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT FK_SupportTickets_Contracts FOREIGN KEY (ContractID)
        REFERENCES dbo.Contracts(ContractID),
    CONSTRAINT FK_SupportTickets_AssignedTo FOREIGN KEY (AssignedToUserID)
        REFERENCES dbo.Users(UserID),
    CONSTRAINT CK_SupportTickets_Category CHECK (
        Category IN (N'BANK_INFO', N'PAYMENT', N'REFUND', N'CONTRACT', N'ACCOUNT', N'OTHER')
    ),
    CONSTRAINT CK_SupportTickets_Status CHECK (
        Status IN (N'OPEN', N'IN_PROGRESS', N'RESOLVED', N'REJECTED')
    ),
    CONSTRAINT CK_SupportTickets_Priority CHECK (
        Priority IN (N'LOW', N'NORMAL', N'HIGH')
    )
);

CREATE INDEX IX_SupportTickets_Status_CreatedAt
ON dbo.Support_Tickets(Status, CreatedAt DESC);

CREATE INDEX IX_SupportTickets_User_CreatedAt
ON dbo.Support_Tickets(UserID, CreatedAt DESC);

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


/* ============================================================
   2. Demo users, roles, customers, employees, drivers, and base cars
   ============================================================ */

USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'CarRentalDB does not exist. Run sql/setup-database.sql from the beginning.', 16, 1);
    RETURN;
END;
GO

USE CarRentalDB;
GO

IF OBJECT_ID(N'dbo.Users', N'U') IS NULL OR OBJECT_ID(N'dbo.Cars', N'U') IS NULL
BEGIN
    RAISERROR(N'Required tables do not exist. Run sql/setup-database.sql from the beginning.', 16, 1);
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

-- Demo users. Legacy plaintext demo passwords are accepted once and auto-upgraded to PBKDF2 on login.
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


/* ============================================================
   3. Low-value PayOS test cars
   ============================================================ */

USE CarRentalDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.Cars', N'U') IS NULL OR OBJECT_ID(N'dbo.Car_Types', N'U') IS NULL
BEGIN
    THROW 50001, 'Cars or Car_Types table does not exist. Run sql/setup-database.sql from the beginning.', 1;
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


/* ============================================================
   4. Extra fleet quantity demo cars
   ============================================================ */

USE CarRentalDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.Cars', N'U') IS NULL OR OBJECT_ID(N'dbo.Car_Types', N'U') IS NULL
BEGIN
    THROW 50001, 'Cars or Car_Types table does not exist. Run sql/setup-database.sql from the beginning.', 1;
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
