USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'Database CarRentalDB does not exist on this SQL Server instance. Run sql/diagnose-sql-connection.sql and connect to the same instance as DBContext.', 16, 1);
    RETURN;
END;
GO

USE CarRentalDB;
GO

SELECT
    cc.name AS ConstraintName,
    cc.definition AS ConstraintDefinition
FROM sys.check_constraints cc
WHERE cc.parent_object_id = OBJECT_ID(N'dbo.Contracts')
  AND cc.name = N'CK_Contracts_Status';

SELECT
    CASE WHEN OBJECT_ID(N'dbo.Payment_Transactions', N'U') IS NULL
         THEN N'MISSING'
         ELSE N'OK'
    END AS PaymentTransactionsTable;

SELECT
    CASE WHEN OBJECT_ID(N'dbo.Refunds', N'U') IS NULL
         THEN N'MISSING'
         ELSE N'OK'
    END AS RefundsTable;

SELECT TOP 20 Status, COUNT(*) AS Total
FROM dbo.Contracts
GROUP BY Status
ORDER BY Status;
GO
