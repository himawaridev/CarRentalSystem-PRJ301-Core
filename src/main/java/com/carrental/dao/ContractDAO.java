package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.Contract;
import com.carrental.model.ContractDetail;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractDAO {

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
            case "ACCEPTED" -> "BOOKED";
            case "REJECTED", "CANCELLED" -> "CANCELLED";
            case "CAR_PICKED_UP" -> "PICKED_UP";
            case "CAR_RETURNED" -> "RETURNED";
            default -> null;
        };
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
