/* CarRentalSystem PRJ301 Core database health check. */

SELECT
    @@SERVERNAME AS ServerName,
    DB_NAME() AS CurrentDatabase,
    SUSER_SNAME() AS LoginName;

IF DB_ID(N'CarRentalCore') IS NULL
BEGIN
    RAISERROR(N'Database CarRentalCore does not exist. Run sql/setup-database.sql first.', 16, 1);
    RETURN;
END;
GO

USE CarRentalCore;
GO

SELECT expected.TableName,
       CASE WHEN actual.object_id IS NULL THEN N'MISSING' ELSE N'OK' END AS Status
FROM (VALUES
    (N'Users'), (N'User_Roles'), (N'Customers'), (N'Cars'),
    (N'Contracts'), (N'Contract_Details'), (N'Payments'),
    (N'Drivers'), (N'Driver_Assignments')
) expected(TableName)
LEFT JOIN sys.tables actual ON actual.name = expected.TableName
ORDER BY expected.TableName;

SELECT Status, COUNT(*) AS TotalCars
FROM dbo.Cars
GROUP BY Status
ORDER BY Status;

SELECT Status, COUNT(*) AS TotalContracts
FROM dbo.Contracts
GROUP BY Status
ORDER BY Status;
GO
