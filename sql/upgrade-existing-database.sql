/*
    CarRentalSystem upgrade script for an existing old CarRentalDB.
    Use this only when the database was created before the latest schema changes.
    New machines should run sql/setup-database.sql instead.
*/


/* ============================================================
   1. Payment contract and transaction refactor
   ============================================================ */

USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'Database CarRentalDB does not exist on this SQL Server instance. Connect to localhost,1433 with the same login used by DBContext, or run sql/setup-database.sql first for a fresh database.', 16, 1);
    RETURN;
END;
GO

USE CarRentalDB;
GO

IF OBJECT_ID(N'dbo.Contracts', N'U') IS NULL
BEGIN
    RAISERROR(N'dbo.Contracts does not exist in CarRentalDB. This migration is only for an existing old schema. For a new database, run sql/setup-database.sql first.', 16, 1);
    RETURN;
END;
GO

UPDATE dbo.Contracts
SET PickupLocation = COALESCE(NULLIF(LTRIM(RTRIM(PickupLocation)), N''), N'Chua cap nhat'),
    ReturnLocation = COALESCE(NULLIF(LTRIM(RTRIM(ReturnLocation)), N''), N'Chua cap nhat');
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Contracts_Status')
    ALTER TABLE dbo.Contracts DROP CONSTRAINT CK_Contracts_Status;
GO

DECLARE @contractStatusDefault SYSNAME;
SELECT @contractStatusDefault = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID(N'dbo.Contracts')
  AND c.name = N'Status';

IF @contractStatusDefault IS NOT NULL
    EXEC(N'ALTER TABLE dbo.Contracts DROP CONSTRAINT [' + @contractStatusDefault + N']');
GO

UPDATE dbo.Contracts
SET Status = CASE Status
    WHEN N'PENDING_REVIEW' THEN N'PENDING_PAYMENT'
    WHEN N'ACCEPTED' THEN N'PENDING_PAYMENT'
    WHEN N'REJECTED' THEN N'CANCELLED'
    WHEN N'DEPOSIT_PAID' THEN N'RESERVED'
    WHEN N'FINAL_PAYMENT_COMPLETED' THEN N'COMPLETED'
    ELSE Status
END;
GO

ALTER TABLE dbo.Contracts ALTER COLUMN PickupLocation NVARCHAR(255) NOT NULL;
ALTER TABLE dbo.Contracts ALTER COLUMN ReturnLocation NVARCHAR(255) NOT NULL;
ALTER TABLE dbo.Contracts ADD CONSTRAINT DF_Contracts_Status DEFAULT N'PENDING_PAYMENT' FOR Status;
ALTER TABLE dbo.Contracts ADD CONSTRAINT CK_Contracts_Status CHECK (
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
);
GO

IF OBJECT_ID(N'dbo.Payment_Transactions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Payment_Transactions (
        PaymentTransactionID BIGINT IDENTITY(1,1) PRIMARY KEY,
        ContractID BIGINT NOT NULL,
        Provider NVARCHAR(40) NOT NULL,
        ProviderTransactionRef NVARCHAR(100) NOT NULL UNIQUE,
        ProviderPaymentRef NVARCHAR(100) NULL,
        Amount DECIMAL(12,2) NOT NULL,
        Status NVARCHAR(30) NOT NULL DEFAULT N'PENDING',
        QrPayload NVARCHAR(1000) NULL,
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
END
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Payments_Type')
    ALTER TABLE dbo.Payments DROP CONSTRAINT CK_Payments_Type;
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Payments_Status')
    ALTER TABLE dbo.Payments DROP CONSTRAINT CK_Payments_Status;
GO

IF COL_LENGTH(N'dbo.Payments', N'PaymentTransactionID') IS NULL
    ALTER TABLE dbo.Payments ADD PaymentTransactionID BIGINT NULL;
IF COL_LENGTH(N'dbo.Payments', N'SourcePaymentID') IS NULL
    ALTER TABLE dbo.Payments ADD SourcePaymentID BIGINT NULL;
GO

UPDATE dbo.Payments
SET PaymentType = CASE PaymentType
    WHEN N'FINAL' THEN N'RENTAL_BALANCE'
    WHEN N'OTHER' THEN N'EXTRA_CHARGE'
    ELSE PaymentType
END,
PaymentStatus = CASE PaymentStatus
    WHEN N'CANCELLED' THEN N'FAILED'
    ELSE PaymentStatus
END;
GO

ALTER TABLE dbo.Payments ALTER COLUMN PaymentType NVARCHAR(30) NOT NULL;
ALTER TABLE dbo.Payments ALTER COLUMN PaymentStatus NVARCHAR(30) NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_Payments_PaymentTransactions')
    ALTER TABLE dbo.Payments ADD CONSTRAINT FK_Payments_PaymentTransactions
        FOREIGN KEY (PaymentTransactionID) REFERENCES dbo.Payment_Transactions(PaymentTransactionID);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_Payments_SourcePayment')
    ALTER TABLE dbo.Payments ADD CONSTRAINT FK_Payments_SourcePayment
        FOREIGN KEY (SourcePaymentID) REFERENCES dbo.Payments(PaymentID);
GO

ALTER TABLE dbo.Payments ADD CONSTRAINT CK_Payments_Type CHECK (
    PaymentType IN (
        N'DEPOSIT',
        N'RENTAL_PREPAID',
        N'DRIVER_FEE_PREPAID',
        N'RENTAL_BALANCE',
        N'EXTRA_CHARGE',
        N'REFUND'
    )
);
ALTER TABLE dbo.Payments ADD CONSTRAINT CK_Payments_Status CHECK (
    PaymentStatus IN (
        N'PENDING',
        N'PAID',
        N'FAILED',
        N'EXPIRED',
        N'REFUND_PENDING',
        N'REFUNDED',
        N'PARTIALLY_REFUNDED'
    )
);
GO

IF OBJECT_ID(N'dbo.Refunds', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Refunds (
        RefundID BIGINT IDENTITY(1,1) PRIMARY KEY,
        ContractID BIGINT NOT NULL,
        SourcePaymentID BIGINT NOT NULL,
        DepositAmount DECIMAL(12,2) NOT NULL,
        DeductionAmount DECIMAL(12,2) NOT NULL DEFAULT 0,
        RefundAmount DECIMAL(12,2) NOT NULL,
        Reason NVARCHAR(500) NULL,
        Status NVARCHAR(30) NOT NULL DEFAULT N'REFUND_PENDING',
        ApprovedByUserID INT NULL,
        ProviderRefundRef NVARCHAR(100) NULL,
        CreatedAt DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2(0) NULL,
        CONSTRAINT FK_Refunds_Contracts FOREIGN KEY (ContractID)
            REFERENCES dbo.Contracts(ContractID),
        CONSTRAINT FK_Refunds_SourcePayment FOREIGN KEY (SourcePaymentID)
            REFERENCES dbo.Payments(PaymentID),
        CONSTRAINT FK_Refunds_ApprovedBy FOREIGN KEY (ApprovedByUserID)
            REFERENCES dbo.Users(UserID),
        CONSTRAINT CK_Refunds_Amounts CHECK (
            DepositAmount >= 0 AND DeductionAmount >= 0 AND RefundAmount >= 0
        ),
        CONSTRAINT CK_Refunds_Status CHECK (
            Status IN (N'REFUND_PENDING', N'REFUNDED', N'FAILED')
        )
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PaymentTransactions_Contract_Status')
    CREATE INDEX IX_PaymentTransactions_Contract_Status
    ON dbo.Payment_Transactions(ContractID, Status);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Refunds_Contract')
    CREATE INDEX IX_Refunds_Contract
    ON dbo.Refunds(ContractID);
GO

ALTER TRIGGER dbo.TR_ContractDetails_PreventCarOverlap
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

ALTER TRIGGER dbo.TR_Contracts_PreventScheduleConflicts
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
            AND cd2.ContractDetailID <> cd1.ContractDetailID
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

ALTER TRIGGER dbo.TR_DriverAssignments_Validate
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


/* ============================================================
   2. Gateway payment, webhook, and manual refund fields
   ============================================================ */

USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'Database CarRentalDB does not exist. Run sql/setup-database.sql first.', 16, 1);
    RETURN;
END;
GO

USE CarRentalDB;
GO

IF OBJECT_ID(N'dbo.Payment_Transactions', N'U') IS NULL
BEGIN
    RAISERROR(N'dbo.Payment_Transactions does not exist. Run sql/upgrade-existing-database.sql from the beginning first.', 16, 1);
    RETURN;
END;
GO

IF COL_LENGTH(N'dbo.Payment_Transactions', N'ProviderOrderCode') IS NULL
    ALTER TABLE dbo.Payment_Transactions ADD ProviderOrderCode BIGINT NULL;
IF COL_LENGTH(N'dbo.Payment_Transactions', N'ProviderCheckoutUrl') IS NULL
    ALTER TABLE dbo.Payment_Transactions ADD ProviderCheckoutUrl NVARCHAR(500) NULL;
IF COL_LENGTH(N'dbo.Payment_Transactions', N'ProviderQrCode') IS NULL
    ALTER TABLE dbo.Payment_Transactions ADD ProviderQrCode NVARCHAR(MAX) NULL;
GO

IF OBJECT_ID(N'dbo.Payment_Webhook_Events', N'U') IS NULL
BEGIN
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
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_PaymentTransactions_ProviderOrderCode')
    CREATE UNIQUE INDEX UX_PaymentTransactions_ProviderOrderCode
    ON dbo.Payment_Transactions(ProviderOrderCode)
    WHERE ProviderOrderCode IS NOT NULL;
GO

IF COL_LENGTH(N'dbo.Refunds', N'RefundMethod') IS NULL
    ALTER TABLE dbo.Refunds ADD RefundMethod NVARCHAR(30) NOT NULL
        CONSTRAINT DF_Refunds_RefundMethod DEFAULT N'GATEWAY_REFUND';
IF COL_LENGTH(N'dbo.Refunds', N'ProofOfRefund') IS NULL
    ALTER TABLE dbo.Refunds ADD ProofOfRefund NVARCHAR(1000) NULL;
IF COL_LENGTH(N'dbo.Refunds', N'CompletedByUserID') IS NULL
    ALTER TABLE dbo.Refunds ADD CompletedByUserID INT NULL;
IF COL_LENGTH(N'dbo.Refunds', N'CompletedAt') IS NULL
    ALTER TABLE dbo.Refunds ADD CompletedAt DATETIME2(0) NULL;
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Refunds_Method')
    ALTER TABLE dbo.Refunds DROP CONSTRAINT CK_Refunds_Method;
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Refunds_Status')
    ALTER TABLE dbo.Refunds DROP CONSTRAINT CK_Refunds_Status;
GO

ALTER TABLE dbo.Refunds ADD CONSTRAINT CK_Refunds_Method CHECK (
    RefundMethod IN (
        N'GATEWAY_REFUND',
        N'CASH_AT_COUNTER',
        N'MANUAL_BANK_TRANSFER',
        N'WALLET_CREDIT'
    )
);
ALTER TABLE dbo.Refunds ADD CONSTRAINT CK_Refunds_Status CHECK (
    Status IN (N'REFUND_PENDING', N'REFUNDED', N'FAILED')
);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_Refunds_CompletedBy')
    ALTER TABLE dbo.Refunds ADD CONSTRAINT FK_Refunds_CompletedBy
        FOREIGN KEY (CompletedByUserID) REFERENCES dbo.Users(UserID);
GO


/* ============================================================
   3. Customer bank account fields
   ============================================================ */

USE CarRentalDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH(N'dbo.Users', N'BankCode') IS NULL
    ALTER TABLE dbo.Users ADD BankCode NVARCHAR(30) NULL;

IF COL_LENGTH(N'dbo.Users', N'BankName') IS NULL
    ALTER TABLE dbo.Users ADD BankName NVARCHAR(120) NULL;

IF COL_LENGTH(N'dbo.Users', N'BankAccountNumber') IS NULL
    ALTER TABLE dbo.Users ADD BankAccountNumber NVARCHAR(30) NULL;

IF COL_LENGTH(N'dbo.Users', N'BankAccountHolder') IS NULL
    ALTER TABLE dbo.Users ADD BankAccountHolder NVARCHAR(120) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Users_BankAccountNumber_NotNull')
    CREATE INDEX IX_Users_BankAccountNumber_NotNull
    ON dbo.Users(BankAccountNumber)
    WHERE BankAccountNumber IS NOT NULL;
GO


/* ============================================================
   4. Support ticket table
   ============================================================ */

USE CarRentalDB;
GO

IF OBJECT_ID(N'dbo.Support_Tickets', N'U') IS NULL
BEGIN
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
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_SupportTickets_Status_CreatedAt'
      AND object_id = OBJECT_ID(N'dbo.Support_Tickets')
)
BEGIN
    CREATE INDEX IX_SupportTickets_Status_CreatedAt
    ON dbo.Support_Tickets(Status, CreatedAt DESC);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_SupportTickets_User_CreatedAt'
      AND object_id = OBJECT_ID(N'dbo.Support_Tickets')
)
BEGIN
    CREATE INDEX IX_SupportTickets_User_CreatedAt
    ON dbo.Support_Tickets(UserID, CreatedAt DESC);
END
GO


/* ============================================================
   5. Authentication security: email verification, reset codes, OAuth
   ============================================================ */

USE CarRentalDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH(N'dbo.Users', N'EmailVerified') IS NULL
    ALTER TABLE dbo.Users ADD EmailVerified BIT NOT NULL
        CONSTRAINT DF_Users_EmailVerified DEFAULT 1;
GO

IF COL_LENGTH(N'dbo.Users', N'EmailVerifiedAt') IS NULL
    ALTER TABLE dbo.Users ADD EmailVerifiedAt DATETIME2(0) NULL;
GO

IF COL_LENGTH(N'dbo.Users', N'AuthProvider') IS NULL
    ALTER TABLE dbo.Users ADD AuthProvider NVARCHAR(30) NULL;
GO

IF COL_LENGTH(N'dbo.Users', N'AuthProviderSubject') IS NULL
    ALTER TABLE dbo.Users ADD AuthProviderSubject NVARCHAR(120) NULL;
GO

UPDATE dbo.Users
SET EmailVerified = 1,
    EmailVerifiedAt = COALESCE(EmailVerifiedAt, CreatedAt, SYSUTCDATETIME())
WHERE EmailVerified = 1
  AND EmailVerifiedAt IS NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_Users_AuthProviderSubject_NotNull'
      AND object_id = OBJECT_ID(N'dbo.Users')
)
BEGIN
    CREATE UNIQUE INDEX UX_Users_AuthProviderSubject_NotNull
    ON dbo.Users(AuthProvider, AuthProviderSubject)
    WHERE AuthProvider IS NOT NULL AND AuthProviderSubject IS NOT NULL;
END
GO

IF OBJECT_ID(N'dbo.Pending_Registrations', N'U') IS NULL
BEGIN
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
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_PendingRegistrations_Username'
      AND object_id = OBJECT_ID(N'dbo.Pending_Registrations')
)
BEGIN
    CREATE UNIQUE INDEX UX_PendingRegistrations_Username
    ON dbo.Pending_Registrations(Username);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_PendingRegistrations_Email'
      AND object_id = OBJECT_ID(N'dbo.Pending_Registrations')
)
BEGIN
    CREATE UNIQUE INDEX UX_PendingRegistrations_Email
    ON dbo.Pending_Registrations(Email);
END
GO

IF OBJECT_ID(N'dbo.Password_Reset_Codes', N'U') IS NULL
BEGIN
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
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_PasswordResetCodes_User_Active'
      AND object_id = OBJECT_ID(N'dbo.Password_Reset_Codes')
)
BEGIN
    CREATE INDEX IX_PasswordResetCodes_User_Active
    ON dbo.Password_Reset_Codes(UserID, ConsumedAt, CreatedAt DESC);
END
GO
