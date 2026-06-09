SELECT
    @@SERVERNAME AS ServerName,
    SERVERPROPERTY('InstanceName') AS InstanceName,
    SERVERPROPERTY('MachineName') AS MachineName,
    DB_NAME() AS CurrentDatabase,
    SUSER_SNAME() AS LoginName;

SELECT name AS DatabaseName
FROM sys.databases
ORDER BY name;
