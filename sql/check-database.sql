/*
    CarRentalSystem database check script.
    Run this when NetBeans/Tomcat can connect but the app reports missing payment/refund schema.
*/


/* ============================================================
   1. SQL Server and database visibility
   ============================================================ */

SELECT
    @@SERVERNAME AS ServerName,
    SERVERPROPERTY('InstanceName') AS InstanceName,
    SERVERPROPERTY('MachineName') AS MachineName,
    DB_NAME() AS CurrentDatabase,
    SUSER_SNAME() AS LoginName;

SELECT name AS DatabaseName
FROM sys.databases
ORDER BY name;


/* ============================================================
   2. CarRentalDB payment/refund schema checks
   ============================================================ */

USE master;
GO

IF DB_ID(N'CarRentalDB') IS NULL
BEGIN
    RAISERROR(N'Database CarRentalDB does not exist on this SQL Server instance. Run sql/check-database.sql and connect to the same instance as DBContext.', 16, 1);
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
