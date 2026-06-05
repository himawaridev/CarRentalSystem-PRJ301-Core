package com.carrental.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility for SQL Server.
 * Update USERNAME and PASSWORD before running.
 */
public class DBContext {

    // IMPORTANT: SQL Server must have TCP/IP enabled on port 1433
    // Open SQL Server Configuration Manager -> Protocols -> Enable TCP/IP -> Restart SQL Server
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB;encrypt=true;trustServerCertificate=true;loginTimeout=10;";

    private static final String USERNAME = "sa";
    private static final String PASSWORD = "Dung22102003@@@";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
