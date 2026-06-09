package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.Contract;
import com.carrental.model.ContractDetail;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentMode;
import com.carrental.model.PaymentTransaction;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractDAO {
    private String lastErrorMessage;

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public boolean createContractWithDetails(Contract contract, List<ContractDetail> details) {
        String insertContract = "INSERT INTO dbo.Contracts (ContractCode,CustomerID,PickupAt,ReturnAt,"
            + "PickupLocation,ReturnLocation,DepositAmountDue,FinalAmountDue) VALUES (?,?,?,?,?,?,?,?)";
        String insertDetail = "INSERT INTO dbo.Contract_Details (ContractID,CarID,RequiresDriver,"
            + "RentalDailyRate,DriverDailyRate,EstimatedDays,RentalAmount,DriverAmount) VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long contractId;
                String code = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                try (PreparedStatement ps = conn.prepareStatement(insertContract, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, code);
                    ps.setInt(2, contract.getCustomerId());
                    ps.setTimestamp(3, Timestamp.valueOf(contract.getPickupAt()));
                    ps.setTimestamp(4, Timestamp.valueOf(contract.getReturnAt()));
                    ps.setString(5, contract.getPickupLocation());
                    ps.setString(6, contract.getReturnLocation());
                    ps.setBigDecimal(7, contract.getDepositAmountDue());
                    ps.setBigDecimal(8, contract.getFinalAmountDue());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        contractId = keys.getLong(1);
                    }
                }
                for (ContractDetail d : details) {
                    try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                        ps.setLong(1, contractId);
                        ps.setInt(2, d.getCarId());
                        ps.setBoolean(3, d.isRequiresDriver());
                        ps.setBigDecimal(4, d.getRentalDailyRate());
                        ps.setBigDecimal(5, d.getDriverDailyRate());
                        ps.setBigDecimal(6, d.getEstimatedDays());
                        ps.setBigDecimal(7, d.getRentalAmount());
                        ps.setBigDecimal(8, d.getDriverAmount());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public PaymentTransaction createPendingBookingWithDetailsAndPayments(
            Contract contract,
            List<ContractDetail> details,
            PaymentMode paymentMode) {

        lastErrorMessage = null;

        String insertContract = "INSERT INTO dbo.Contracts (ContractCode, CustomerID, PickupAt, ReturnAt, "
            + "PickupLocation, ReturnLocation, Status, DepositAmountDue, FinalAmountDue) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertDetail = "INSERT INTO dbo.Contract_Details (ContractID, CarID, RequiresDriver, "
            + "RentalDailyRate, DriverDailyRate, EstimatedDays, RentalAmount, DriverAmount) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String code = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                long contractId;

                try (PreparedStatement ps = conn.prepareStatement(insertContract, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, code);
                    ps.setInt(2, contract.getCustomerId());
                    ps.setTimestamp(3, Timestamp.valueOf(contract.getPickupAt()));
                    ps.setTimestamp(4, Timestamp.valueOf(contract.getReturnAt()));
                    ps.setString(5, contract.getPickupLocation());
                    ps.setString(6, contract.getReturnLocation());
                    ps.setString(7, ContractStatus.PENDING_PAYMENT);
                    ps.setBigDecimal(8, contract.getDepositAmountDue());
                    ps.setBigDecimal(9, contract.getFinalAmountDue());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Cannot create contract.");
                        }
                        contractId = keys.getLong(1);
                    }
                }

                BigDecimal rentalAmount = BigDecimal.ZERO;
                BigDecimal driverFeeAmount = BigDecimal.ZERO;
                for (ContractDetail d : details) {
                    try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                        ps.setLong(1, contractId);
                        ps.setInt(2, d.getCarId());
                        ps.setBoolean(3, d.isRequiresDriver());
                        ps.setBigDecimal(4, d.getRentalDailyRate());
                        ps.setBigDecimal(5, d.getDriverDailyRate());
                        ps.setBigDecimal(6, d.getEstimatedDays());
                        ps.setBigDecimal(7, d.getRentalAmount());
                        ps.setBigDecimal(8, d.getDriverAmount());
                        ps.executeUpdate();
                    }
                    rentalAmount = rentalAmount.add(safe(d.getRentalAmount()));
                    driverFeeAmount = driverFeeAmount.add(safe(d.getDriverAmount()));
                }

                insertStatusHistory(conn, contractId, null, ContractStatus.PENDING_PAYMENT,
                        "Booking created and waiting for deposit payment.");

                PaymentDAO paymentDAO = new PaymentDAO();
                PaymentTransaction paymentTransaction = paymentDAO.createPendingPayments(
                        conn,
                        contractId,
                        code,
                        contract.getDepositAmountDue(),
                        rentalAmount,
                        driverFeeAmount,
                        paymentMode);

                contract.setContractId(contractId);
                contract.setContractCode(code);
                contract.setStatus(ContractStatus.PENDING_PAYMENT);
                conn.commit();
                return paymentTransaction;
            } catch (SQLException e) {
                conn.rollback();
                lastErrorMessage = toBookingErrorMessage(e);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            lastErrorMessage = toBookingErrorMessage(e);
            e.printStackTrace();
        }
        return null;
    }

    public List<Contract> getContractsByStatus(String status) {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT co.*, u.FullName AS CustomerName, u.Phone AS CustomerPhone "
            + "FROM dbo.Contracts co "
            + "INNER JOIN dbo.Customers cu ON co.CustomerID = cu.CustomerID "
            + "INNER JOIN dbo.Users u ON cu.UserID = u.UserID "
            + "WHERE co.Status = ? ORDER BY co.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapContract(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Contract> getAllContracts() {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT co.*, u.FullName AS CustomerName, u.Phone AS CustomerPhone "
            + "FROM dbo.Contracts co "
            + "INNER JOIN dbo.Customers cu ON co.CustomerID = cu.CustomerID "
            + "INNER JOIN dbo.Users u ON cu.UserID = u.UserID "
            + "ORDER BY co.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapContract(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Contract> getContractsByCustomerId(int customerId) {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT co.*, u.FullName AS CustomerName, u.Phone AS CustomerPhone "
            + "FROM dbo.Contracts co "
            + "INNER JOIN dbo.Customers cu ON co.CustomerID = cu.CustomerID "
            + "INNER JOIN dbo.Users u ON cu.UserID = u.UserID "
            + "WHERE co.CustomerID = ? ORDER BY co.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapContract(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Contract getContractById(long contractId) {
        String sql = "SELECT co.*, u.FullName AS CustomerName, u.Phone AS CustomerPhone "
            + "FROM dbo.Contracts co "
            + "INNER JOIN dbo.Customers cu ON co.CustomerID = cu.CustomerID "
            + "INNER JOIN dbo.Users u ON cu.UserID = u.UserID "
            + "WHERE co.ContractID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapContract(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<ContractDetail> getDetailsByContractId(long contractId) {
        List<ContractDetail> list = new ArrayList<>();
        String sql = "SELECT cd.*, c.Brand AS CarBrand, c.Model AS CarModel, c.LicensePlate, ct.SeatCount "
            + "FROM dbo.Contract_Details cd "
            + "INNER JOIN dbo.Cars c ON cd.CarID = c.CarID "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID "
            + "WHERE cd.ContractID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContractDetail d = new ContractDetail();
                    d.setContractDetailId(rs.getLong("ContractDetailID"));
                    d.setContractId(rs.getLong("ContractID"));
                    d.setCarId(rs.getInt("CarID"));
                    d.setRequiresDriver(rs.getBoolean("RequiresDriver"));
                    d.setRentalDailyRate(rs.getBigDecimal("RentalDailyRate"));
                    d.setDriverDailyRate(rs.getBigDecimal("DriverDailyRate"));
                    d.setEstimatedDays(rs.getBigDecimal("EstimatedDays"));
                    d.setRentalAmount(rs.getBigDecimal("RentalAmount"));
                    d.setDriverAmount(rs.getBigDecimal("DriverAmount"));
                    d.setLineTotal(rs.getBigDecimal("LineTotal"));
                    d.setDetailStatus(rs.getString("DetailStatus"));
                    d.setCarBrand(rs.getString("CarBrand"));
                    d.setCarModel(rs.getString("CarModel"));
                    d.setLicensePlate(rs.getString("LicensePlate"));
                    d.setSeatCount(rs.getInt("SeatCount"));
                    list.add(d);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateContractStatus(long contractId, String newStatus, int staffUserId) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get old status
                String oldStatus = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT Status FROM dbo.Contracts WHERE ContractID = ?")) {
                    ps.setLong(1, contractId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) oldStatus = rs.getString("Status");
                    }
                }
                // Update contract
                String updateSql = "UPDATE dbo.Contracts SET Status=?, ReviewedByUserID=?, "
                    + "ReviewedAt=SYSUTCDATETIME(), UpdatedAt=SYSUTCDATETIME() WHERE ContractID=?";
                if (!isAllowedTransition(oldStatus, newStatus)) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, staffUserId);
                    ps.setLong(3, contractId);
                    ps.executeUpdate();
                }
                // Update detail statuses
                String detailStatus = mapDetailStatus(newStatus);
                if (detailStatus != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE dbo.Contract_Details SET DetailStatus=? "
                            + "WHERE ContractID=? AND DetailStatus <> N'CANCELLED'")) {
                        ps.setString(1, detailStatus);
                        ps.setLong(2, contractId);
                        ps.executeUpdate();
                    }
                }
                // Insert history
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dbo.Contract_Status_History (ContractID,OldStatus,NewStatus,ChangedByUserID) "
                        + "VALUES (?,?,?,?)")) {
                    ps.setLong(1, contractId);
                    ps.setString(2, oldStatus);
                    ps.setString(3, newStatus);
                    ps.setInt(4, staffUserId);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private String mapDetailStatus(String contractStatus) {
        return switch (contractStatus) {
            case "RESERVED", "CONFIRMED" -> "BOOKED";
            case "PAYMENT_EXPIRED", "CANCELLED" -> "CANCELLED";
            case "CAR_PICKED_UP" -> "PICKED_UP";
            case "CAR_RETURNED", "SETTLEMENT_PENDING", "COMPLETED" -> "RETURNED";
            default -> null;
        };
    }

    private boolean isAllowedTransition(String oldStatus, String newStatus) {
        if (oldStatus == null || oldStatus.equals(newStatus)) {
            return true;
        }
        return switch (oldStatus) {
            case "PENDING_PAYMENT" -> newStatus.equals("PAYMENT_EXPIRED") || newStatus.equals("CANCELLED");
            case "RESERVED" -> newStatus.equals("CONFIRMED") || newStatus.equals("CANCELLED");
            case "CONFIRMED" -> newStatus.equals("CAR_PICKED_UP") || newStatus.equals("CANCELLED");
            case "CAR_PICKED_UP" -> newStatus.equals("CAR_RETURNED");
            case "CAR_RETURNED" -> newStatus.equals("SETTLEMENT_PENDING") || newStatus.equals("COMPLETED");
            case "SETTLEMENT_PENDING" -> newStatus.equals("COMPLETED");
            default -> false;
        };
    }

    private void insertStatusHistory(Connection conn, long contractId, String oldStatus, String newStatus, String note)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO dbo.Contract_Status_History (ContractID,OldStatus,NewStatus,Note) "
                + "VALUES (?,?,?,?)")) {
            ps.setLong(1, contractId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String toBookingErrorMessage(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Dat xe that bai do loi co so du lieu.";
        }
        if (message.contains("CK_Contracts_Status")
                || message.contains("Payment_Transactions")
                || message.contains("Payment_Webhook_Events")
                || message.contains("ProviderOrderCode")
                || message.contains("ProviderCheckoutUrl")
                || message.contains("CK_Payments_Type")
                || message.contains("CK_Payments_Status")) {
            return "Database chua cap nhat migration thanh toan moi. Hay chay sql/payment-refactor-migration.sql va sql/payment-gateway-refund-migration.sql roi thu lai.";
        }
        if (message.contains("Missing PAYOS_CLIENT_ID")
                || message.contains("PAYOS_CLIENT_ID, PAYOS_API_KEY")) {
            return "Chua cau hinh cong thanh toan payOS hoac payOS dang loi. Hay kiem tra PAYOS_CLIENT_ID, PAYOS_API_KEY, PAYOS_CHECKSUM_KEY va APP_BASE_URL.";
        }
        if (message.contains("PAYOS") || message.contains("payOS")) {
            return "PayOS hien chua tao duoc link thanh toan. Chi tiet: " + compactGatewayMessage(message);
        }
        if (message.toLowerCase().contains("overlap")
                || message.toLowerCase().contains("conflict")) {
            return "Dat xe that bai. Xe co the da duoc giu boi don hang khac trong cung thoi gian.";
        }
        return "Dat xe that bai do loi co so du lieu: " + message;
    }

    private String compactGatewayMessage(String message) {
        String clean = message == null ? "" : message
                .replace("Cannot create real payment link from payOS:", "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (clean.isBlank()) {
            return "vui long thu lai sau hoac kiem tra ket noi payOS/ngrok.";
        }
        int maxLength = 240;
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength) + "...";
    }

    private Contract mapContract(ResultSet rs) throws SQLException {
        Contract c = new Contract();
        c.setContractId(rs.getLong("ContractID"));
        c.setContractCode(rs.getString("ContractCode"));
        c.setCustomerId(rs.getInt("CustomerID"));
        Timestamp p = rs.getTimestamp("PickupAt");
        if (p != null) c.setPickupAt(p.toLocalDateTime());
        Timestamp r = rs.getTimestamp("ReturnAt");
        if (r != null) c.setReturnAt(r.toLocalDateTime());
        c.setPickupLocation(rs.getString("PickupLocation"));
        c.setReturnLocation(rs.getString("ReturnLocation"));
        c.setStatus(rs.getString("Status"));
        c.setDepositAmountDue(rs.getBigDecimal("DepositAmountDue"));
        c.setFinalAmountDue(rs.getBigDecimal("FinalAmountDue"));
        Timestamp ca = rs.getTimestamp("CreatedAt");
        if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        try {
            c.setCustomerName(rs.getString("CustomerName"));
            c.setCustomerPhone(rs.getString("CustomerPhone"));
        } catch (SQLException ignored) {}
        return c;
    }
}
