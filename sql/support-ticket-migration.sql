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
