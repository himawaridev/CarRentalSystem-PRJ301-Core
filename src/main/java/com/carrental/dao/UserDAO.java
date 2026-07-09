package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.User;
import com.carrental.service.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Users, Customers, Roles, and authentication.
 */
public class UserDAO {

    static final String USER_COLUMNS = "UserID, Username, Email, PasswordHash, FullName, Phone, "
            + "Address, IdentityNumber, Status, EmailVerified, EmailVerifiedAt, AuthProvider, AuthProviderSubject, CreatedAt";
    static final String USER_COLUMNS_QUALIFIED = "u.UserID, u.Username, u.Email, u.PasswordHash, "
            + "u.FullName, u.Phone, u.Address, u.IdentityNumber, u.Status, u.EmailVerified, u.EmailVerifiedAt, "
            + "u.AuthProvider, u.AuthProviderSubject, u.CreatedAt";

    /**
     * Authenticate by username/password. Legacy plaintext demo passwords are upgraded after a successful login.
     */
    public User login(String username, String password) {
        String sql = "SELECT " + USER_COLUMNS + " "
                + "FROM dbo.Users WHERE Username = ? AND Status = N'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    if (!user.isEmailVerified()) {
                        return null;
                    }
                    if (PasswordHasher.verify(password, user.getPasswordHash())) {
                        return user;
                    }
                    if (PasswordHasher.legacyPlainTextMatches(password, user.getPasswordHash())) {
                        String upgradedHash = PasswordHasher.hash(password);
                        updatePasswordHash(conn, user.getUserId(), upgradedHash);
                        user.setPasswordHash(upgradedHash);
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register a new customer: insert User, assign CUSTOMER role, insert Customers record.
     * Uses transaction to ensure atomicity.
     */
    public boolean registerCustomer(User user) {
        String insertUser = "INSERT INTO dbo.Users "
                + "(Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber, EmailVerified, EmailVerifiedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1, SYSUTCDATETIME())";
        String assignRole = "INSERT INTO dbo.User_Roles (UserID, RoleID) "
                + "SELECT ?, RoleID FROM dbo.Roles WHERE RoleName = N'CUSTOMER'";
        String insertCustomer = "INSERT INTO dbo.Customers (UserID) VALUES (?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, hashIfNeeded(user.getPasswordHash()));
                    ps.setString(4, user.getFullName());
                    ps.setString(5, user.getPhone());
                    ps.setString(6, user.getAddress());
                    ps.setString(7, user.getIdentityNumber());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        userId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(assignRole)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertCustomer)) {
                    ps.setInt(1, userId);
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

    /**
     * Get CustomerID by UserID.
     */
    public Integer getCustomerIdByUserId(int userId) {
        String sql = "SELECT CustomerID FROM dbo.Customers WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CustomerID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get DriverID by UserID.
     */
    public Integer getDriverIdByUserId(int userId) {
        String sql = "SELECT DriverID FROM dbo.Drivers WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("DriverID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all role names for a user.
     */
    public List<String> getUserRoles(int userId) {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT r.RoleName FROM dbo.User_Roles ur "
                + "INNER JOIN dbo.Roles r ON ur.RoleID = r.RoleID WHERE ur.UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("RoleName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Get all users (for admin).
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT " + USER_COLUMNS + " "
                + "FROM dbo.Users ORDER BY CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Get user by ID.
     */
    public User getUserById(int userId) {
        String sql = "SELECT " + USER_COLUMNS + " "
                + "FROM dbo.Users WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByCustomerId(int customerId) {
        String sql = "SELECT " + USER_COLUMNS_QUALIFIED + " "
                + "FROM dbo.Users u "
                + "INNER JOIN dbo.Customers c ON c.UserID = u.UserID "
                + "WHERE c.CustomerID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateCustomerProfile(
            int userId,
            String fullName,
            String phone,
            String address) {
        String sql = "UPDATE dbo.Users SET FullName = ?, Phone = ?, Address = ?, UpdatedAt = SYSUTCDATETIME() "
                + "WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalize(fullName));
            ps.setString(2, normalize(phone));
            ps.setString(3, normalize(address));
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserByAdmin(User user) {
        String sql = "UPDATE dbo.Users SET Email = ?, FullName = ?, Phone = ?, Address = ?, "
                + "IdentityNumber = ?, Status = ?, UpdatedAt = SYSUTCDATETIME() "
                + "WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalize(user.getEmail()));
            ps.setString(2, normalize(user.getFullName()));
            ps.setString(3, normalize(user.getPhone()));
            ps.setString(4, normalize(user.getAddress()));
            ps.setString(5, normalize(user.getIdentityNumber()));
            ps.setString(6, normalize(user.getStatus()));
            ps.setInt(7, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create a new user with specified roles (for admin).
     */
    public boolean createUser(User user, List<String> roleNames) {
        String insertUser = "INSERT INTO dbo.Users "
                + "(Username, Email, PasswordHash, FullName, Phone, Address, IdentityNumber, Status, EmailVerified, EmailVerifiedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, SYSUTCDATETIME())";
        String assignRole = "INSERT INTO dbo.User_Roles (UserID, RoleID) "
                + "SELECT ?, RoleID FROM dbo.Roles WHERE RoleName = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, hashIfNeeded(user.getPasswordHash()));
                    ps.setString(4, user.getFullName());
                    ps.setString(5, user.getPhone());
                    ps.setString(6, user.getAddress());
                    ps.setString(7, user.getIdentityNumber());
                    ps.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        userId = keys.getInt(1);
                    }
                }
                for (String roleName : roleNames) {
                    try (PreparedStatement ps = conn.prepareStatement(assignRole)) {
                        ps.setInt(1, userId);
                        ps.setString(2, roleName);
                        ps.executeUpdate();
                    }
                    // Create profile records based on role
                    if ("CUSTOMER".equals(roleName)) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO dbo.Customers (UserID) VALUES (?)")) {
                            ps.setInt(1, userId);
                            ps.executeUpdate();
                        }
                    }
                    if ("DRIVER".equals(roleName)) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO dbo.Drivers (UserID, LicenseNumber, LicenseClass, LicenseExpiryDate) "
                                + "VALUES (?, ?, N'B2', DATEADD(YEAR, 5, GETDATE()))")) {
                            ps.setInt(1, userId);
                            ps.setString(2, "DL-" + userId);
                            ps.executeUpdate();
                        }
                    }
                    if ("STAFF".equals(roleName) || "MANAGER".equals(roleName) || "ADMIN".equals(roleName)) {
                        // Check if employee record already exists
                        boolean exists;
                        try (PreparedStatement psc = conn.prepareStatement(
                                "SELECT 1 FROM dbo.Employees WHERE UserID = ?")) {
                            psc.setInt(1, userId);
                            try (ResultSet rsc = psc.executeQuery()) {
                                exists = rsc.next();
                            }
                        }
                        if (!exists) {
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "INSERT INTO dbo.Employees (UserID, EmployeeCode) VALUES (?, ?)")) {
                                ps.setInt(1, userId);
                                ps.setString(2, "EMP-" + userId);
                                ps.executeUpdate();
                            }
                        }
                    }
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

    /**
     * Update user status (ACTIVE, LOCKED, DISABLED).
     */
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE dbo.Users SET Status = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePasswordHash(int userId, String passwordHash) {
        try (Connection conn = DBContext.getConnection()) {
            return updatePasswordHash(conn, userId, passwordHash);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update user roles (remove existing, assign new).
     */
    public boolean updateUserRoles(int userId, List<String> roleNames) {
        String deleteRoles = "DELETE FROM dbo.User_Roles WHERE UserID = ?";
        String assignRole = "INSERT INTO dbo.User_Roles (UserID, RoleID) "
                + "SELECT ?, RoleID FROM dbo.Roles WHERE RoleName = ?";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteRoles)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                for (String roleName : roleNames) {
                    try (PreparedStatement ps = conn.prepareStatement(assignRole)) {
                        ps.setInt(1, userId);
                        ps.setString(2, roleName);
                        ps.executeUpdate();
                    }
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

    /**
     * Get all available roles.
     */
    public List<String> getAllRoleNames() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT RoleName FROM dbo.Roles ORDER BY RoleID";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(rs.getString("RoleName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Check if username exists.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM dbo.Users WHERE Username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if email exists.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM dbo.Users WHERE Email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    static User mapUserRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("UserID"));
        u.setUsername(rs.getString("Username"));
        u.setEmail(rs.getString("Email"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setFullName(rs.getString("FullName"));
        u.setPhone(rs.getString("Phone"));
        u.setAddress(rs.getString("Address"));
        u.setIdentityNumber(rs.getString("IdentityNumber"));
        u.setStatus(rs.getString("Status"));
        u.setEmailVerified(readBoolean(rs, "EmailVerified", true));
        u.setAuthProvider(readNullable(rs, "AuthProvider"));
        u.setAuthProviderSubject(readNullable(rs, "AuthProviderSubject"));
        try {
            Timestamp ts = rs.getTimestamp("EmailVerifiedAt");
            if (ts != null) u.setEmailVerifiedAt(ts.toLocalDateTime());
        } catch (SQLException ignored) {}
        try {
            Timestamp ts = rs.getTimestamp("CreatedAt");
            if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        } catch (SQLException ignored) {}
        return u;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return mapUserRow(rs);
    }

    private static String readNullable(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException ignored) {
            return null;
        }
    }

    private static boolean readBoolean(ResultSet rs, String column, boolean defaultValue) {
        try {
            boolean value = rs.getBoolean(column);
            return rs.wasNull() ? defaultValue : value;
        } catch (SQLException ignored) {
            return defaultValue;
        }
    }

    private boolean updatePasswordHash(Connection conn, int userId, String passwordHash) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Users SET PasswordHash = ?, UpdatedAt = SYSUTCDATETIME() WHERE UserID = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private String hashIfNeeded(String value) {
        if (PasswordHasher.isHashed(value)) {
            return value;
        }
        return PasswordHasher.hash(value == null ? "" : value);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

}
