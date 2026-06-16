package com.carrental.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AuthConfig {
    private static final String LOCAL_CONFIG_PATH = "config/auth-local.properties";
    private static final String DEFAULT_APP_BASE_URL = "http://localhost:9999/CarRentalSystem";

    public String appBaseUrl() {
        return trimTrailingSlash(config("APP_BASE_URL", DEFAULT_APP_BASE_URL));
    }

    public boolean devMode() {
        return Boolean.parseBoolean(config("AUTH_DEV_MODE", "false"));
    }

    public String smtpHost() {
        return config("SMTP_HOST", "");
    }

    public int smtpPort() {
        return parseInt(config("SMTP_PORT", "587"), 587);
    }

    public String smtpUsername() {
        return config("SMTP_USERNAME", "");
    }

    public String smtpPassword() {
        return config("SMTP_PASSWORD", "");
    }

    public String smtpFrom() {
        String from = config("SMTP_FROM", "");
        return from.isBlank() ? smtpUsername() : from;
    }

    public boolean smtpStartTls() {
        return Boolean.parseBoolean(config("SMTP_STARTTLS", "true"));
    }

    public boolean smtpSsl() {
        return Boolean.parseBoolean(config("SMTP_SSL", "false"));
    }

    public boolean smtpConfigured() {
        return isFilled(smtpHost()) && isFilled(smtpUsername())
                && isFilled(smtpPassword()) && isFilled(smtpFrom());
    }

    public String googleClientId() {
        return config("GOOGLE_CLIENT_ID", "");
    }

    public String googleClientSecret() {
        return config("GOOGLE_CLIENT_SECRET", "");
    }

    public String facebookClientId() {
        return config("FACEBOOK_CLIENT_ID", "");
    }

    public String facebookClientSecret() {
        return config("FACEBOOK_CLIENT_SECRET", "");
    }

    public String facebookGraphVersion() {
        return config("FACEBOOK_GRAPH_VERSION", "v25.0");
    }

    public boolean googleConfigured() {
        return isFilled(googleClientId()) && isFilled(googleClientSecret());
    }

    public boolean facebookConfigured() {
        return isFilled(facebookClientId()) && isFilled(facebookClientSecret());
    }

    public boolean providerConfigured(String provider) {
        return "google".equals(provider) ? googleConfigured()
                : "facebook".equals(provider) && facebookConfigured();
    }

    public String redirectUri(String provider) {
        return appBaseUrl() + "/oauth/" + provider + "/callback";
    }

    private static String config(String envName, String defaultValue) {
        String fileValue = localConfig(envName);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        String systemProperty = System.getProperty(envName.toLowerCase().replace('_', '.'));
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
        try (InputStream input = AuthConfig.class.getClassLoader()
                .getResourceAsStream("auth-local.properties")) {
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
                // Local auth config is optional.
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
            paths.add(Path.of(catalinaBase, "conf", "auth-local.properties"));
        }

        try {
            Path classLocation = Path.of(AuthConfig.class.getProtectionDomain()
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

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean isFilled(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        return !normalized.startsWith("your_")
                && !normalized.equals("your-email-app-password")
                && !normalized.equals("your email app password");
    }
}
