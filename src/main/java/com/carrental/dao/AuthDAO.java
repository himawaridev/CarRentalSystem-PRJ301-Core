package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.User;
import com.carrental.service.OAuthProfile;
import com.carrental.service.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

public class AuthDAO {
    private static final int MAX_CODE_ATTEMPTS = 5;

    public enum CodeResult {
        SUCCESS,
        NOT_FOUND,
        EXPIRED,
        TOO_MANY_ATTEMPTS,
        INVALID,
        DUPLICATE,
        FAILED
    }

    public static class PendingRegistration {
        private long id;
        private String username;
        private String email;
        private String passwordHash;
        private String fullName;
        private String phone;
        private String address;
        private String codeHash;
        private LocalDateTime expiresAt;
        private int attempts;

        public long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPasswordHash() { return passwordHash; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public String getCodeHash() { return codeHash; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public int getAttempts() { return attempts; }
    }

    public boolean savePendingRegistration(
            String username,
            String email,
            String passwordHash,
            String fullName,
            String phone,
            String address,
            String codeHash,
            LocalDateTime expiresAt) {
        String deleteSql = "DELETE FROM dbo.Pending_Registrations WHERE Username = ? OR Email = ?";
        String insertSql = "INSERT INTO dbo.Pending_Registrations "
                + "(Username, Email, PasswordHash, FullName, Phone, Address, VerificationCodeHash, ExpiresAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, normalizeUsername(username));
                    ps.setString(2, normalizeEmail(email));
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, normalizeUsername(username));
                    ps.setString(2, normalizeEmail(email));
                    ps.setString(3, passwordHash);
                    ps.setString(4, normalize(fullName));
                    ps.setString(5, normalize(phone));
                    ps.setString(6, normalize(address));
                    ps.setString(7, codeHash);
                    ps.setTimestamp(8, Timestamp.valueOf(expiresAt));
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public PendingRegistration findPendingRegistration(String email) {
        String sql = "SELECT TOP 1 * FROM dbo.Pending_Registrations WHERE Email = ? ORDER BY CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPendingRegistration(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CodeResult completePendingRegistration(String email, String code) {
        String selectSql = "SELECT TOP 1 * FROM dbo.Pending_Registrations WITH (UPDLOCK, ROWLOCK) "
                + "WHERE Email = ? ORDER BY CreatedAt DESC";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PendingRegistration pending;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, normalizeEmail(email));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return CodeResult.NOT_FOUND;
                        }
                        pending = mapPendingRegistration(rs);
                    }
                }

                if (pending.getAttempts() >= MAX_CODE_ATTEMPTS) {
                    conn.rollback();
                    return CodeResult.TOO_MANY_ATTEMPTS;
                }
                if (isExpired(pending.getExpiresAt())) {
                    conn.rollback();
                    return CodeResult.EXPIRED;
                }
                if (!PasswordHasher.verify(code, pending.getCodeHash())) {
                    incrementPendingAttempts(conn, pending.getId());
                    conn.commit();
                    return CodeResult.INVALID;
                }
                if (userExists(conn, pending.getUsername(), pending.getEmail())) {
                    conn.rollback();
                    return CodeResult.DUPLICATE;
                }

                int userId = insertCustomerUser(
                        conn,
                        pending.getUsername(),
                        pending.getEmail(),
                        pending.getPasswordHash(),
                        pending.getFullName(),
                        pending.getPhone(),
                        pending.getAddress(),
                        null,
                        null,
                        true);
                assignCustomerRole(conn, userId);
                insertCustomer(conn, userId);
                deletePending(conn, pending.getId());
                conn.commit();
                return CodeResult.SUCCESS;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CodeResult.FAILED;
    }

    public boolean savePasswordResetCode(String email, String codeHash, LocalDateTime expiresAt) {
        String findUser = "SELECT UserID FROM dbo.Users WHERE Email = ? AND Status = N'ACTIVE'";
        String insertCode = "INSERT INTO dbo.Password_Reset_Codes (UserID, CodeHash, ExpiresAt) VALUES (?, ?, ?)";
        try (Connection conn = DBContext.getConnection()) {
            Integer userId = null;
            try (PreparedStatement ps = conn.prepareStatement(findUser)) {
                ps.setString(1, normalizeEmail(email));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("UserID");
                    }
                }
            }
            if (userId == null) {
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement(insertCode)) {
                ps.setInt(1, userId);
                ps.setString(2, codeHash);
                ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public CodeResult resetPassword(String email, String code, String newPasswordHash) {
        String selectSql = "SELECT TOP 1 pr.*, u.UserID "
                + "FROM dbo.Password_Reset_Codes pr WITH (UPDLOCK, ROWLOCK) "
                + "JOIN dbo.Users u ON u.UserID = pr.UserID "
                + "WHERE u.Email = ? AND u.Status = N'ACTIVE' AND pr.ConsumedAt IS NULL "
                + "ORDER BY pr.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long resetId;
                int userId;
                String codeHash;
                LocalDateTime expiresAt;
                int attempts;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, normalizeEmail(email));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return CodeResult.NOT_FOUND;
                        }
                        resetId = rs.getLong("PasswordResetID");
                        userId = rs.getInt("UserID");
                        codeHash = rs.getString("CodeHash");
                        expiresAt = rs.getTimestamp("ExpiresAt").toLocalDateTime();
                        attempts = rs.getInt("Attempts");
                    }
                }

                if (attempts >= MAX_CODE_ATTEMPTS) {
                    conn.rollback();
                    return CodeResult.TOO_MANY_ATTEMPTS;
                }
                if (isExpired(expiresAt)) {
                    conn.rollback();
                    return CodeResult.EXPIRED;
                }
                if (!PasswordHasher.verify(code, codeHash)) {
                    incrementResetAttempts(conn, resetId);
                    conn.commit();
                    return CodeResult.INVALID;
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dbo.Users SET PasswordHash = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?")) {
                    ps.setString(1, newPasswordHash);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dbo.Password_Reset_Codes SET ConsumedAt = SYSUTCDATETIME() WHERE PasswordResetID = ?")) {
                    ps.setLong(1, resetId);
                    ps.executeUpdate();
                }
                conn.commit();
                return CodeResult.SUCCESS;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CodeResult.FAILED;
    }

    public User loginOrCreateOAuthUser(OAuthProfile profile) {
        String selectByProvider = "SELECT " + UserDAO.USER_COLUMNS_QUALIFIED + " "
                + "FROM dbo.Users u WHERE u.AuthProvider = ? AND u.AuthProviderSubject = ? AND u.Status = N'ACTIVE'";
        String selectByEmail = "SELECT " + UserDAO.USER_COLUMNS_QUALIFIED + " "
                + "FROM dbo.Users u WITH (UPDLOCK, ROWLOCK) WHERE u.Email = ? AND u.Status = N'ACTIVE'";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(selectByProvider)) {
                    ps.setString(1, profile.getProvider());
                    ps.setString(2, profile.getSubject());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            User user = UserDAO.mapUserRow(rs);
                            conn.commit();
                            return user;
                        }
                    }
                }

                if (isBlank(profile.getEmail()) || !profile.isEmailVerified()) {
                    conn.rollback();
                    return null;
                }

                try (PreparedStatement ps = conn.prepareStatement(selectByEmail)) {
                    ps.setString(1, normalizeEmail(profile.getEmail()));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            User user = UserDAO.mapUserRow(rs);
                            linkOAuth(conn, user.getUserId(), profile);
                            conn.commit();
                            user.setAuthProvider(profile.getProvider());
                            user.setAuthProviderSubject(profile.getSubject());
                            user.setEmailVerified(true);
                            return user;
                        }
                    }
                }

                String username = uniqueOAuthUsername(conn, profile);
                int userId = insertCustomerUser(
                        conn,
                        username,
                        normalizeEmail(profile.getEmail()),
                        PasswordHasher.hash(PasswordHasher.randomUrlToken()),
                        fallbackName(profile),
                        null,
                        null,
                        profile.getProvider(),
                        profile.getSubject(),
                        true);
                assignCustomerRole(conn, userId);
                insertCustomer(conn, userId);
                User user = findUserById(conn, userId);
                conn.commit();
                return user;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findActiveUserByEmail(String email) {
        String sql = "SELECT " + UserDAO.USER_COLUMNS_QUALIFIED + " "
                + "FROM dbo.Users u WHERE u.Email = ? AND u.Status = N'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UserDAO.mapUserRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int insertCustomerUser(
            Connection conn,
            String username,
            String email,
            String passwordHash,
            String fullName,
            String phone,
            String address,
            String authProvider,
            String authProviderSubject,
            boolean emailVerified) throws SQLException {
        String sql = "INSERT INTO dbo.Users "
                + "(Username, Email, PasswordHash, FullName, Phone, Address, AuthProvider, AuthProviderSubject, "
                + "EmailVerified, EmailVerifiedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CASE WHEN ? = 1 THEN SYSUTCDATETIME() ELSE NULL END)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, normalizeUsername(username));
            ps.setString(2, normalizeEmail(email));
            ps.setString(3, passwordHash);
            ps.setString(4, normalize(fullName));
            ps.setString(5, normalize(phone));
            ps.setString(6, normalize(address));
            ps.setString(7, normalize(authProvider));
            ps.setString(8, normalize(authProviderSubject));
            ps.setBoolean(9, emailVerified);
            ps.setBoolean(10, emailVerified);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Cannot create user.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void assignCustomerRole(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO dbo.User_Roles (UserID, RoleID) "
                + "SELECT ?, RoleID FROM dbo.Roles WHERE RoleName = N'CUSTOMER'")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void insertCustomer(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.Customers (UserID) VALUES (?)")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void linkOAuth(Connection conn, int userId, OAuthProfile profile) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Users SET AuthProvider = ?, AuthProviderSubject = ?, "
                + "EmailVerified = 1, EmailVerifiedAt = COALESCE(EmailVerifiedAt, SYSUTCDATETIME()), "
                + "UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?")) {
            ps.setString(1, profile.getProvider());
            ps.setString(2, profile.getSubject());
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    private User findUserById(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT " + UserDAO.USER_COLUMNS + " FROM dbo.Users WHERE UserID = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? UserDAO.mapUserRow(rs) : null;
            }
        }
    }

    private boolean userExists(Connection conn, String username, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM dbo.Users WHERE Username = ? OR Email = ?")) {
            ps.setString(1, normalizeUsername(username));
            ps.setString(2, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String uniqueOAuthUsername(Connection conn, OAuthProfile profile) throws SQLException {
        String prefix = normalizeUsername(profile.getEmail().split("@")[0])
                .replaceAll("[^A-Za-z0-9_]", "");
        if (prefix.isBlank()) {
            prefix = profile.getProvider() + "_user";
        }
        prefix = prefix.length() > 28 ? prefix.substring(0, 28) : prefix;

        String candidate = prefix;
        int suffix = 1;
        while (usernameExists(conn, candidate)) {
            candidate = prefix + "_" + suffix++;
        }
        return candidate;
    }

    private boolean usernameExists(Connection conn, String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM dbo.Users WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void incrementPendingAttempts(Connection conn, long pendingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Pending_Registrations SET Attempts = Attempts + 1, "
                + "UpdatedAt = SYSUTCDATETIME() WHERE PendingRegistrationID = ?")) {
            ps.setLong(1, pendingId);
            ps.executeUpdate();
        }
    }

    private void incrementResetAttempts(Connection conn, long resetId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Password_Reset_Codes SET Attempts = Attempts + 1 WHERE PasswordResetID = ?")) {
            ps.setLong(1, resetId);
            ps.executeUpdate();
        }
    }

    private void deletePending(Connection conn, long pendingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dbo.Pending_Registrations WHERE PendingRegistrationID = ?")) {
            ps.setLong(1, pendingId);
            ps.executeUpdate();
        }
    }

    private PendingRegistration mapPendingRegistration(ResultSet rs) throws SQLException {
        PendingRegistration pending = new PendingRegistration();
        pending.id = rs.getLong("PendingRegistrationID");
        pending.username = rs.getString("Username");
        pending.email = rs.getString("Email");
        pending.passwordHash = rs.getString("PasswordHash");
        pending.fullName = rs.getString("FullName");
        pending.phone = rs.getString("Phone");
        pending.address = rs.getString("Address");
        pending.codeHash = rs.getString("VerificationCodeHash");
        pending.expiresAt = rs.getTimestamp("ExpiresAt").toLocalDateTime();
        pending.attempts = rs.getInt("Attempts");
        return pending;
    }

    private boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now(ZoneOffset.UTC));
    }

    private String fallbackName(OAuthProfile profile) {
        if (!isBlank(profile.getFullName())) {
            return profile.getFullName();
        }
        return profile.getProvider().substring(0, 1).toUpperCase(Locale.ROOT)
                + profile.getProvider().substring(1) + " User";
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeEmail(String value) {
        return normalize(value) == null ? null : normalize(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String value) {
        return normalize(value) == null ? null : normalize(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
