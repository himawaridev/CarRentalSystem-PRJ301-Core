USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'Database CarRentalDB does not exist. Run sql/database-schema.sql first.', 16, 1);
    RETURN;
END;
GO

USE CarRentalDB;
GO

IF OBJECT_ID(N'dbo.Payment_Transactions', N'U') IS NULL
BEGIN
    RAISERROR(N'dbo.Payment_Transactions does not exist. Run sql/payment-refactor-migration.sql first.', 16, 1);
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
