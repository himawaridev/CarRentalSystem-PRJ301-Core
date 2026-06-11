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
