package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.Driver;
import com.carrental.model.DriverAssignment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDAO {

    public List<Driver> getActiveDrivers() {
        List<Driver> list = new ArrayList<>();
        String sql = "SELECT d.*, u.FullName, u.Phone FROM dbo.Drivers d "
            + "INNER JOIN dbo.Users u ON d.UserID = u.UserID "
            + "WHERE d.EmploymentStatus = N'ACTIVE' ORDER BY u.FullName";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapDriver(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Get active drivers with busy/free status for a specific contract date range.
     * Drivers who have assignments overlapping with the given date range are marked busy.
     */
    public List<Driver> getDriversWithAvailability(java.time.LocalDateTime pickupAt, java.time.LocalDateTime returnAt) {
        List<Driver> list = new ArrayList<>();
        String sql = "SELECT d.*, u.FullName, u.Phone, "
            + "CASE WHEN EXISTS ("
            + "  SELECT 1 FROM dbo.Driver_Assignments da "
            + "  INNER JOIN dbo.Contract_Details cd ON da.ContractDetailID = cd.ContractDetailID "
            + "  INNER JOIN dbo.Contracts c ON cd.ContractID = c.ContractID "
            + "  WHERE da.DriverID = d.DriverID "
            + "    AND da.AssignmentStatus NOT IN (N'CANCELLED', N'TRIP_COMPLETED') "
            + "    AND c.Status NOT IN (N'PENDING_PAYMENT', N'PAYMENT_EXPIRED', N'CANCELLED', N'COMPLETED') "
            + "    AND c.PickupAt < ? AND c.ReturnAt > ? "
            + ") THEN 1 ELSE 0 END AS IsBusy "
            + "FROM dbo.Drivers d "
            + "INNER JOIN dbo.Users u ON d.UserID = u.UserID "
            + "WHERE d.EmploymentStatus = N'ACTIVE' "
            + "ORDER BY IsBusy ASC, u.FullName";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(returnAt));
            ps.setTimestamp(2, Timestamp.valueOf(pickupAt));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Driver d = mapDriver(rs);
                    d.setBusy(rs.getInt("IsBusy") == 1);
                    list.add(d);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Get all active (non-cancelled, non-completed) driver assignments.
     */
    public List<DriverAssignment> getActiveAssignments() {
        List<DriverAssignment> list = new ArrayList<>();
        String sql = "SELECT da.*, u.FullName AS DriverName, "
            + "c.Brand AS CarBrand, c.Model AS CarModel, c.LicensePlate, "
            + "con.ContractCode, con.PickupAt, con.ReturnAt, "
            + "cu.CustomerID, u2.FullName AS CustomerName "
            + "FROM dbo.Driver_Assignments da "
            + "INNER JOIN dbo.Drivers dr ON da.DriverID = dr.DriverID "
            + "INNER JOIN dbo.Users u ON dr.UserID = u.UserID "
            + "INNER JOIN dbo.Contract_Details cd ON da.ContractDetailID = cd.ContractDetailID "
            + "INNER JOIN dbo.Cars c ON cd.CarID = c.CarID "
            + "INNER JOIN dbo.Contracts con ON cd.ContractID = con.ContractID "
            + "INNER JOIN dbo.Customers cu ON con.CustomerID = cu.CustomerID "
            + "INNER JOIN dbo.Users u2 ON cu.UserID = u2.UserID "
            + "WHERE da.AssignmentStatus NOT IN (N'CANCELLED', N'TRIP_COMPLETED') "
            + "AND con.Status NOT IN (N'PENDING_PAYMENT', N'PAYMENT_EXPIRED', N'CANCELLED', N'COMPLETED') "
            + "ORDER BY con.PickupAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DriverAssignment a = new DriverAssignment();
                a.setAssignmentId(rs.getLong("AssignmentID"));
                a.setContractDetailId(rs.getLong("ContractDetailID"));
                a.setDriverId(rs.getInt("DriverID"));
                a.setAssignmentStatus(rs.getString("AssignmentStatus"));
                a.setDriverNote(rs.getString("DriverNote"));
                a.setCarBrand(rs.getString("CarBrand"));
                a.setCarModel(rs.getString("CarModel"));
                a.setLicensePlate(rs.getString("LicensePlate"));
                a.setContractCode(rs.getString("ContractCode"));
                a.setCustomerName(rs.getString("CustomerName"));
                Timestamp p = rs.getTimestamp("PickupAt");
                if (p != null) a.setPickupAt(p.toLocalDateTime());
                Timestamp r = rs.getTimestamp("ReturnAt");
                if (r != null) a.setReturnAt(r.toLocalDateTime());
                // Store driver name in driverNote temporarily (reuse field)
                a.setDriverName(rs.getString("DriverName"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Check if a contract detail already has a driver assigned (non-cancelled).
     */
    public DriverAssignment getAssignmentByDetailId(long contractDetailId) {
        String sql = "SELECT da.*, u.FullName AS DriverName FROM dbo.Driver_Assignments da "
            + "INNER JOIN dbo.Drivers dr ON da.DriverID = dr.DriverID "
            + "INNER JOIN dbo.Users u ON dr.UserID = u.UserID "
            + "WHERE da.ContractDetailID = ? AND da.AssignmentStatus <> N'CANCELLED'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractDetailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DriverAssignment a = new DriverAssignment();
                    a.setAssignmentId(rs.getLong("AssignmentID"));
                    a.setDriverId(rs.getInt("DriverID"));
                    a.setAssignmentStatus(rs.getString("AssignmentStatus"));
                    a.setDriverName(rs.getString("DriverName"));
                    return a;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean assignDriver(long contractDetailId, int driverId, int assignedByUserId) {
        String sql = "INSERT INTO dbo.Driver_Assignments (ContractDetailID,DriverID,AssignedByUserID) VALUES (?,?,?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractDetailId);
            ps.setInt(2, driverId);
            ps.setInt(3, assignedByUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<DriverAssignment> getScheduleByDriverId(int driverId) {
        List<DriverAssignment> list = new ArrayList<>();
        String sql = "SELECT da.*, c.Brand AS CarBrand, c.Model AS CarModel, c.LicensePlate, "
            + "u.FullName AS CustomerName, con.PickupAt, con.ReturnAt, "
            + "con.PickupLocation, con.ReturnLocation, con.ContractCode "
            + "FROM dbo.Driver_Assignments da "
            + "INNER JOIN dbo.Contract_Details cd ON da.ContractDetailID = cd.ContractDetailID "
            + "INNER JOIN dbo.Cars c ON cd.CarID = c.CarID "
            + "INNER JOIN dbo.Contracts con ON cd.ContractID = con.ContractID "
            + "INNER JOIN dbo.Customers cu ON con.CustomerID = cu.CustomerID "
            + "INNER JOIN dbo.Users u ON cu.UserID = u.UserID "
            + "WHERE da.DriverID = ? AND da.AssignmentStatus <> N'CANCELLED' "
            + "ORDER BY con.PickupAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DriverAssignment a = new DriverAssignment();
                    a.setAssignmentId(rs.getLong("AssignmentID"));
                    a.setContractDetailId(rs.getLong("ContractDetailID"));
                    a.setDriverId(rs.getInt("DriverID"));
                    a.setAssignmentStatus(rs.getString("AssignmentStatus"));
                    Timestamp t = rs.getTimestamp("AssignedAt");
                    if (t != null) a.setAssignedAt(t.toLocalDateTime());
                    a.setDriverNote(rs.getString("DriverNote"));
                    a.setCarBrand(rs.getString("CarBrand"));
                    a.setCarModel(rs.getString("CarModel"));
                    a.setLicensePlate(rs.getString("LicensePlate"));
                    a.setCustomerName(rs.getString("CustomerName"));
                    Timestamp p = rs.getTimestamp("PickupAt");
                    if (p != null) a.setPickupAt(p.toLocalDateTime());
                    Timestamp r = rs.getTimestamp("ReturnAt");
                    if (r != null) a.setReturnAt(r.toLocalDateTime());
                    a.setPickupLocation(rs.getString("PickupLocation"));
                    a.setReturnLocation(rs.getString("ReturnLocation"));
                    a.setContractCode(rs.getString("ContractCode"));
                    list.add(a);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateAssignmentStatus(long assignmentId, String status) {
        String sql;
        if ("TRIP_COMPLETED".equals(status)) {
            sql = "UPDATE dbo.Driver_Assignments SET AssignmentStatus=?, TripCompletedAt=SYSUTCDATETIME() WHERE AssignmentID=?";
        } else if ("HANDOVER_RECEIVED".equals(status)) {
            sql = "UPDATE dbo.Driver_Assignments SET AssignmentStatus=?, HandoverReceivedAt=SYSUTCDATETIME() WHERE AssignmentID=?";
        } else {
            sql = "UPDATE dbo.Driver_Assignments SET AssignmentStatus=? WHERE AssignmentID=?";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Driver mapDriver(ResultSet rs) throws SQLException {
        Driver d = new Driver();
        d.setDriverId(rs.getInt("DriverID"));
        d.setUserId(rs.getInt("UserID"));
        d.setLicenseNumber(rs.getString("LicenseNumber"));
        d.setLicenseClass(rs.getString("LicenseClass"));
        d.setBaseDailyFee(rs.getBigDecimal("BaseDailyFee"));
        d.setEmploymentStatus(rs.getString("EmploymentStatus"));
        d.setFullName(rs.getString("FullName"));
        d.setPhone(rs.getString("Phone"));
        return d;
    }
}
