package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.SupportTicket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SupportTicketDAO {
    private static final Set<String> CATEGORIES = Set.of(
            "BANK_INFO", "PAYMENT", "REFUND", "CONTRACT", "ACCOUNT", "OTHER");
    private static final Set<String> STATUSES = Set.of("OPEN", "IN_PROGRESS", "RESOLVED", "REJECTED");
    private static final Set<String> PRIORITIES = Set.of("LOW", "NORMAL", "HIGH");

    public List<SupportTicket> getTicketsByUserId(int userId) {
        ensureSchema();
        List<SupportTicket> tickets = new ArrayList<>();
        String sql = baseSelect()
                + "WHERE st.UserID = ? "
                + "ORDER BY st.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<SupportTicket> getAllTickets(String statusFilter) {
        ensureSchema();
        List<SupportTicket> tickets = new ArrayList<>();
        boolean filter = isValidStatus(statusFilter);
        String sql = baseSelect()
                + (filter ? "WHERE st.Status = ? " : "")
                + "ORDER BY CASE st.Status "
                + "WHEN N'OPEN' THEN 1 WHEN N'IN_PROGRESS' THEN 2 WHEN N'RESOLVED' THEN 3 ELSE 4 END, "
                + "st.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filter) {
                ps.setString(1, statusFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public boolean createTicket(SupportTicket ticket) {
        ensureSchema();
        String sql = "INSERT INTO dbo.Support_Tickets "
                + "(TicketCode, UserID, ContractID, Category, Subject, Message, Status, Priority) "
                + "VALUES (?, ?, ?, ?, ?, ?, N'OPEN', N'NORMAL')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newTicketCode());
            ps.setInt(2, ticket.getUserId());
            if (ticket.getContractId() == null) {
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(3, ticket.getContractId());
            }
            ps.setString(4, normalizeCategory(ticket.getCategory()));
            ps.setString(5, trimTo(ticket.getSubject(), 150));
            ps.setString(6, trimTo(ticket.getMessage(), 1000));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTicketByStaff(
            long ticketId,
            String status,
            String priority,
            String staffResponse,
            int staffUserId) {
        ensureSchema();
        if (!isValidStatus(status) || !isValidPriority(priority)) {
            return false;
        }
        String sql = "UPDATE dbo.Support_Tickets SET "
                + "Status = ?, Priority = ?, StaffResponse = ?, AssignedToUserID = ?, "
                + "ResolvedAt = CASE WHEN ? IN (N'RESOLVED', N'REJECTED') THEN SYSUTCDATETIME() ELSE NULL END, "
                + "UpdatedAt = SYSUTCDATETIME() "
                + "WHERE TicketID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, priority);
            ps.setString(3, normalize(staffResponse));
            ps.setInt(4, staffUserId);
            ps.setString(5, status);
            ps.setLong(6, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isValidCategory(String category) {
        return category != null && CATEGORIES.contains(category);
    }

    public boolean isValidStatus(String status) {
        return status != null && STATUSES.contains(status);
    }

    public boolean isValidPriority(String priority) {
        return priority != null && PRIORITIES.contains(priority);
    }

    private String baseSelect() {
        return "SELECT st.*, u.FullName AS CustomerName, u.Email AS CustomerEmail, u.Phone AS CustomerPhone, "
                + "co.ContractCode, au.FullName AS AssignedStaffName "
                + "FROM dbo.Support_Tickets st "
                + "INNER JOIN dbo.Users u ON st.UserID = u.UserID "
                + "LEFT JOIN dbo.Contracts co ON st.ContractID = co.ContractID "
                + "LEFT JOIN dbo.Users au ON st.AssignedToUserID = au.UserID ";
    }

    private SupportTicket mapTicket(ResultSet rs) throws SQLException {
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketId(rs.getLong("TicketID"));
        ticket.setTicketCode(rs.getString("TicketCode"));
        ticket.setUserId(rs.getInt("UserID"));
        long contractId = rs.getLong("ContractID");
        ticket.setContractId(rs.wasNull() ? null : contractId);
        ticket.setCategory(rs.getString("Category"));
        ticket.setSubject(rs.getString("Subject"));
        ticket.setMessage(rs.getString("Message"));
        ticket.setStatus(rs.getString("Status"));
        ticket.setPriority(rs.getString("Priority"));
        ticket.setStaffResponse(rs.getString("StaffResponse"));
        int assignedTo = rs.getInt("AssignedToUserID");
        ticket.setAssignedToUserId(rs.wasNull() ? null : assignedTo);
        ticket.setResolvedAt(toLocalDateTime(rs.getTimestamp("ResolvedAt")));
        ticket.setCreatedAt(toLocalDateTime(rs.getTimestamp("CreatedAt")));
        ticket.setUpdatedAt(toLocalDateTime(rs.getTimestamp("UpdatedAt")));
        ticket.setCustomerName(rs.getString("CustomerName"));
        ticket.setCustomerEmail(rs.getString("CustomerEmail"));
        ticket.setCustomerPhone(rs.getString("CustomerPhone"));
        ticket.setContractCode(rs.getString("ContractCode"));
        ticket.setAssignedStaffName(rs.getString("AssignedStaffName"));
        return ticket;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String normalizeCategory(String value) {
        return isValidCategory(value) ? value : "OTHER";
    }

    private String newTicketCode() {
        String timestamp = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "SUP" + timestamp + suffix;
    }

    private String trimTo(String value, int maxLength) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "";
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private void ensureSchema() {
        String createTable = """
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
                        CONSTRAINT FK_SupportTickets_Users FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
                        CONSTRAINT FK_SupportTickets_Contracts FOREIGN KEY (ContractID) REFERENCES dbo.Contracts(ContractID),
                        CONSTRAINT FK_SupportTickets_AssignedTo FOREIGN KEY (AssignedToUserID) REFERENCES dbo.Users(UserID),
                        CONSTRAINT CK_SupportTickets_Category CHECK (Category IN (N'BANK_INFO', N'PAYMENT', N'REFUND', N'CONTRACT', N'ACCOUNT', N'OTHER')),
                        CONSTRAINT CK_SupportTickets_Status CHECK (Status IN (N'OPEN', N'IN_PROGRESS', N'RESOLVED', N'REJECTED')),
                        CONSTRAINT CK_SupportTickets_Priority CHECK (Priority IN (N'LOW', N'NORMAL', N'HIGH'))
                    )
                END
                """;
        String createStatusIndex = """
                IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_SupportTickets_Status_CreatedAt'
                    AND object_id = OBJECT_ID(N'dbo.Support_Tickets'))
                    CREATE INDEX IX_SupportTickets_Status_CreatedAt
                    ON dbo.Support_Tickets(Status, CreatedAt DESC)
                """;
        String createUserIndex = """
                IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_SupportTickets_User_CreatedAt'
                    AND object_id = OBJECT_ID(N'dbo.Support_Tickets'))
                    CREATE INDEX IX_SupportTickets_User_CreatedAt
                    ON dbo.Support_Tickets(UserID, CreatedAt DESC)
                """;
        try (Connection conn = DBContext.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(createTable);
            st.execute(createStatusIndex);
            st.execute(createUserIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
