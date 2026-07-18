package com.carrental.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Database connection utility for SQL Server.
 * Uses the Microsoft JDBC driver and loads local credentials from a properties file,
 * Java system properties, or environment variables.
 */
public class DBContext {

    // IMPORTANT: SQL Server must have TCP/IP enabled on port 1433
    // Open SQL Server Configuration Manager -> Protocols -> Enable TCP/IP -> Restart SQL Server
    private static final String LOCAL_CONFIG_PATH = "config/database-local.properties";
    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=CarRentalCore;encrypt=true;trustServerCertificate=true;loginTimeout=10;";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = config("DB_URL", "db.url", DEFAULT_URL);
        String username = requiredConfig("DB_USERNAME", "db.username");
        String password = requiredConfig("DB_PASSWORD", "db.password");
        return DriverManager.getConnection(url, username, password);
    }

    private static String requiredConfig(String envName, String propertyName) throws SQLException {
        String value = config(envName, propertyName, null);
        if (value == null || value.isBlank()) {
            throw new SQLException("Missing database config " + envName
                    + ". Copy src/main/resources/database-local.example.properties to "
                    + "src/main/resources/database-local.properties, or set environment variable "
                    + envName + ".");
        }
        return value;
    }

    private static String config(String envName, String propertyName, String defaultValue) {
        String fileValue = localConfig(envName);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        String systemProperty = System.getProperty(propertyName);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String environmentValue = System.getenv(envName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        return defaultValue;
    }

    private static String localConfig(String key) {
        String fileValue = fileConfig(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }

        Properties properties = new Properties();
        try (InputStream input = DBContext.class.getClassLoader()
                .getResourceAsStream("database-local.properties")) {
            if (input == null) {
                return null;
            }
            properties.load(input);
            return properties.getProperty(key);
        } catch (IOException e) {
            return null;
        }
    }

    private static String fileConfig(String key) {
        for (Path configPath : candidateConfigPaths()) {
            if (!Files.isRegularFile(configPath)) {
                continue;
            }

            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
                String value = properties.getProperty(key);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            } catch (IOException e) {
                // Try the next location. Local config is optional.
            }
        }
        return null;
    }

    private static List<Path> candidateConfigPaths() {
        List<Path> paths = new ArrayList<>();
        paths.add(Path.of(LOCAL_CONFIG_PATH));

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            paths.add(Path.of(catalinaBase, LOCAL_CONFIG_PATH));
            paths.add(Path.of(catalinaBase, "conf", "database-local.properties"));
        }

        try {
            Path classLocation = Path.of(DBContext.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path current = Files.isDirectory(classLocation) ? classLocation : classLocation.getParent();
            for (int i = 0; current != null && i < 8; i++) {
                paths.add(current.resolve(LOCAL_CONFIG_PATH));
                current = current.getParent();
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            // Ignore invalid code source location.
        }

        return paths;
    }
}
