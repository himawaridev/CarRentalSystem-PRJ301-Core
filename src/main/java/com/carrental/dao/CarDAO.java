package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.Car;
import com.carrental.model.CarType;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CarDAO {

    public List<Car> findAvailableCars(Integer seatCount, LocalDateTime pickupAt, LocalDateTime returnAt) {
        return findAvailableCars(seatCount, null, null, null, pickupAt, returnAt);
    }

    public List<Car> findAvailableCars(
            Integer seatCount,
            String brand,
            BigDecimal minDailyRate,
            BigDecimal maxDailyRate,
            LocalDateTime pickupAt,
            LocalDateTime returnAt) {
        List<Car> cars = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "WITH AvailableCars AS ("
            + "SELECT c.*, ct.TypeName, ct.SeatCount "
            + "FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID "
            + "WHERE c.Status = N'AVAILABLE' ");
        if (seatCount != null) sql.append("AND ct.SeatCount = ? ");
        if (brand != null && !brand.isBlank()) sql.append("AND c.Brand = ? ");
        if (minDailyRate != null) sql.append("AND c.DailyRate >= ? ");
        if (maxDailyRate != null) sql.append("AND c.DailyRate <= ? ");
        sql.append("AND NOT EXISTS (SELECT 1 FROM dbo.Contract_Details cd "
            + "INNER JOIN dbo.Contracts con ON cd.ContractID = con.ContractID "
            + "WHERE cd.CarID = c.CarID AND cd.DetailStatus <> N'CANCELLED' "
            + "AND con.Status IN (N'RESERVED',N'CONFIRMED',N'CAR_PICKED_UP') "
            + "AND con.PickupAt < ? AND con.ReturnAt > ?) ");
        sql.append("AND NOT EXISTS (SELECT 1 FROM dbo.Car_Maintenance cm "
            + "WHERE cm.CarID = c.CarID AND cm.Status IN (N'SCHEDULED',N'IN_PROGRESS') "
            + "AND cm.StartAt < ? AND cm.EndAt > ?) ");
        sql.append("), RankedCars AS ("
            + "SELECT *, "
            + "COUNT(*) OVER (PARTITION BY CarTypeID, Brand, Model, ManufactureYear, Transmission, FuelType, DailyRate, DepositAmount) AS AvailableQuantity, "
            + "ROW_NUMBER() OVER (PARTITION BY CarTypeID, Brand, Model, ManufactureYear, Transmission, FuelType, DailyRate, DepositAmount ORDER BY NEWID()) AS GroupRank "
            + "FROM AvailableCars"
            + ") "
            + "SELECT * FROM RankedCars WHERE GroupRank = 1 "
            + "ORDER BY DailyRate ASC, Brand, Model");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (seatCount != null) ps.setInt(idx++, seatCount);
            if (brand != null && !brand.isBlank()) ps.setString(idx++, brand.trim());
            if (minDailyRate != null) ps.setBigDecimal(idx++, minDailyRate);
            if (maxDailyRate != null) ps.setBigDecimal(idx++, maxDailyRate);
            ps.setTimestamp(idx++, Timestamp.valueOf(returnAt));
            ps.setTimestamp(idx++, Timestamp.valueOf(pickupAt));
            ps.setTimestamp(idx++, Timestamp.valueOf(returnAt));
            ps.setTimestamp(idx++, Timestamp.valueOf(pickupAt));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cars.add(mapCar(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cars;
    }

    public Car getCarById(int carId) {
        String sql = "SELECT c.*, ct.TypeName, ct.SeatCount, "
            + "ISNULL(g.AvailableQuantity, 0) AS AvailableQuantity "
            + "FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID "
            + "OUTER APPLY ("
            + "SELECT COUNT(*) AS AvailableQuantity "
            + "FROM dbo.Cars c2 "
            + "WHERE c2.CarTypeID = c.CarTypeID "
            + "AND c2.Brand = c.Brand "
            + "AND c2.Model = c.Model "
            + "AND ISNULL(c2.ManufactureYear, -1) = ISNULL(c.ManufactureYear, -1) "
            + "AND ISNULL(c2.Transmission, N'') = ISNULL(c.Transmission, N'') "
            + "AND ISNULL(c2.FuelType, N'') = ISNULL(c.FuelType, N'') "
            + "AND c2.DailyRate = c.DailyRate "
            + "AND c2.DepositAmount = c.DepositAmount "
            + "AND c2.Status = N'AVAILABLE'"
            + ") g "
            + "WHERE c.CarID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCar(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Car> getCatalogCarGroups() {
        List<Car> cars = new ArrayList<>();
        String sql = "WITH RankedCars AS ("
            + "SELECT c.*, ct.TypeName, ct.SeatCount, "
            + "SUM(CASE WHEN c.Status = N'AVAILABLE' THEN 1 ELSE 0 END) "
            + "OVER (PARTITION BY c.CarTypeID, c.Brand, c.Model, c.ManufactureYear, c.Transmission, c.FuelType, c.DailyRate, c.DepositAmount) AS AvailableQuantity, "
            + "ROW_NUMBER() OVER (PARTITION BY c.CarTypeID, c.Brand, c.Model, c.ManufactureYear, c.Transmission, c.FuelType, c.DailyRate, c.DepositAmount "
            + "ORDER BY CASE WHEN c.Status = N'AVAILABLE' THEN 0 ELSE 1 END, c.CarID DESC) AS GroupRank "
            + "FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID"
            + ") "
            + "SELECT * FROM RankedCars WHERE GroupRank = 1 ORDER BY Brand, Model, DailyRate";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cars.add(mapCar(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return cars;
    }

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "WITH AvailableGroups AS ("
            + "SELECT CarTypeID, Brand, Model, ManufactureYear, Transmission, FuelType, DailyRate, DepositAmount, "
            + "COUNT(*) AS AvailableQuantity "
            + "FROM dbo.Cars "
            + "WHERE Status = N'AVAILABLE' "
            + "GROUP BY CarTypeID, Brand, Model, ManufactureYear, Transmission, FuelType, DailyRate, DepositAmount"
            + ") "
            + "SELECT c.*, ct.TypeName, ct.SeatCount, "
            + "ISNULL(g.AvailableQuantity, 0) AS AvailableQuantity "
            + "FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID "
            + "LEFT JOIN AvailableGroups g ON g.CarTypeID = c.CarTypeID "
            + "AND g.Brand = c.Brand "
            + "AND g.Model = c.Model "
            + "AND ISNULL(g.ManufactureYear, -1) = ISNULL(c.ManufactureYear, -1) "
            + "AND ISNULL(g.Transmission, N'') = ISNULL(c.Transmission, N'') "
            + "AND ISNULL(g.FuelType, N'') = ISNULL(c.FuelType, N'') "
            + "AND g.DailyRate = c.DailyRate "
            + "AND g.DepositAmount = c.DepositAmount "
            + "ORDER BY c.CarID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cars.add(mapCar(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return cars;
    }

    public boolean insertCar(Car car) {
        String sql = "INSERT INTO dbo.Cars (CarTypeID,LicensePlate,Brand,Model,ManufactureYear,Color,"
            + "Transmission,FuelType,Mileage,DailyRate,DepositAmount,Status,ImageUrl,Description) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, car.getCarTypeId());
            ps.setString(2, car.getLicensePlate());
            ps.setString(3, car.getBrand());
            ps.setString(4, car.getModel());
            if (car.getManufactureYear() != null) ps.setShort(5, car.getManufactureYear());
            else ps.setNull(5, Types.SMALLINT);
            ps.setString(6, car.getColor());
            ps.setString(7, car.getTransmission());
            ps.setString(8, car.getFuelType());
            ps.setInt(9, car.getMileage());
            ps.setBigDecimal(10, car.getDailyRate());
            ps.setBigDecimal(11, car.getDepositAmount());
            ps.setString(12, car.getStatus() != null ? car.getStatus() : "AVAILABLE");
            ps.setString(13, car.getImageUrl());
            ps.setString(14, car.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateCar(Car car) {
        String sql = "UPDATE dbo.Cars SET CarTypeID=?,LicensePlate=?,Brand=?,Model=?,ManufactureYear=?,"
            + "Color=?,Transmission=?,FuelType=?,Mileage=?,DailyRate=?,DepositAmount=?,Status=?,"
            + "ImageUrl=?,Description=?,UpdatedAt=SYSUTCDATETIME() WHERE CarID=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, car.getCarTypeId());
            ps.setString(2, car.getLicensePlate());
            ps.setString(3, car.getBrand());
            ps.setString(4, car.getModel());
            if (car.getManufactureYear() != null) ps.setShort(5, car.getManufactureYear());
            else ps.setNull(5, Types.SMALLINT);
            ps.setString(6, car.getColor());
            ps.setString(7, car.getTransmission());
            ps.setString(8, car.getFuelType());
            ps.setInt(9, car.getMileage());
            ps.setBigDecimal(10, car.getDailyRate());
            ps.setBigDecimal(11, car.getDepositAmount());
            ps.setString(12, car.getStatus());
            ps.setString(13, car.getImageUrl());
            ps.setString(14, car.getDescription());
            ps.setInt(15, car.getCarId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public Car findRandomAvailableCarInSameGroup(
            int representativeCarId,
            LocalDateTime pickupAt,
            LocalDateTime returnAt,
            Set<Integer> excludedCarIds) {

        StringBuilder sql = new StringBuilder(
            "WITH CandidateCars AS ("
            + "SELECT candidate.* "
            + "FROM dbo.Cars ref "
            + "INNER JOIN dbo.Cars candidate ON candidate.CarTypeID = ref.CarTypeID "
            + "AND candidate.Brand = ref.Brand "
            + "AND candidate.Model = ref.Model "
            + "AND ISNULL(candidate.ManufactureYear, -1) = ISNULL(ref.ManufactureYear, -1) "
            + "AND ISNULL(candidate.Transmission, N'') = ISNULL(ref.Transmission, N'') "
            + "AND ISNULL(candidate.FuelType, N'') = ISNULL(ref.FuelType, N'') "
            + "AND candidate.DailyRate = ref.DailyRate "
            + "AND candidate.DepositAmount = ref.DepositAmount "
            + "WHERE ref.CarID = ? "
            + "AND candidate.Status = N'AVAILABLE' "
            + "AND NOT EXISTS (SELECT 1 FROM dbo.Contract_Details cd "
            + "INNER JOIN dbo.Contracts con ON cd.ContractID = con.ContractID "
            + "WHERE cd.CarID = candidate.CarID AND cd.DetailStatus <> N'CANCELLED' "
            + "AND con.Status IN (N'RESERVED',N'CONFIRMED',N'CAR_PICKED_UP') "
            + "AND con.PickupAt < ? AND con.ReturnAt > ?) "
            + "AND NOT EXISTS (SELECT 1 FROM dbo.Car_Maintenance cm "
            + "WHERE cm.CarID = candidate.CarID AND cm.Status IN (N'SCHEDULED',N'IN_PROGRESS') "
            + "AND cm.StartAt < ? AND cm.EndAt > ?) ");

        if (excludedCarIds != null && !excludedCarIds.isEmpty()) {
            sql.append("AND candidate.CarID NOT IN (");
            for (int i = 0; i < excludedCarIds.size(); i++) {
                if (i > 0) sql.append(",");
                sql.append("?");
            }
            sql.append(") ");
        }

        sql.append("), RankedCars AS ("
            + "SELECT c.*, ct.TypeName, ct.SeatCount, "
            + "COUNT(*) OVER (PARTITION BY c.CarTypeID, c.Brand, c.Model, c.ManufactureYear, c.Transmission, c.FuelType, c.DailyRate, c.DepositAmount) AS AvailableQuantity, "
            + "ROW_NUMBER() OVER (ORDER BY NEWID()) AS PickRank "
            + "FROM CandidateCars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID"
            + ") "
            + "SELECT * FROM RankedCars WHERE PickRank = 1");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, representativeCarId);
            ps.setTimestamp(idx++, Timestamp.valueOf(returnAt));
            ps.setTimestamp(idx++, Timestamp.valueOf(pickupAt));
            ps.setTimestamp(idx++, Timestamp.valueOf(returnAt));
            ps.setTimestamp(idx++, Timestamp.valueOf(pickupAt));
            if (excludedCarIds != null) {
                for (Integer excludedId : excludedCarIds) {
                    ps.setInt(idx++, excludedId);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCar(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<CarType> getAllCarTypes() {
        List<CarType> types = new ArrayList<>();
        String sql = "SELECT CarTypeID, TypeName, SeatCount, Description FROM dbo.Car_Types ORDER BY SeatCount";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CarType ct = new CarType();
                ct.setCarTypeId(rs.getInt("CarTypeID"));
                ct.setTypeName(rs.getString("TypeName"));
                ct.setSeatCount(rs.getInt("SeatCount"));
                ct.setDescription(rs.getString("Description"));
                types.add(ct);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return types;
    }

    public List<String> getAvailableBrands() {
        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT Brand FROM dbo.Cars "
                + "WHERE Brand IS NOT NULL AND LTRIM(RTRIM(Brand)) <> N'' "
                + "ORDER BY Brand";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                brands.add(rs.getString("Brand"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return brands;
    }

    public List<Integer> getAvailableSeatCounts() {
        List<Integer> seatCounts = new ArrayList<>();
        String sql = "SELECT DISTINCT ct.SeatCount "
                + "FROM dbo.Cars c "
                + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID "
                + "ORDER BY ct.SeatCount";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                seatCounts.add(rs.getInt("SeatCount"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return seatCounts;
    }

    private Car mapCar(ResultSet rs) throws SQLException {
        Car c = new Car();
        c.setCarId(rs.getInt("CarID"));
        c.setCarTypeId(rs.getInt("CarTypeID"));
        c.setLicensePlate(rs.getString("LicensePlate"));
        c.setBrand(rs.getString("Brand"));
        c.setModel(rs.getString("Model"));
        short year = rs.getShort("ManufactureYear");
        c.setManufactureYear(rs.wasNull() ? null : year);
        c.setColor(rs.getString("Color"));
        c.setTransmission(rs.getString("Transmission"));
        c.setFuelType(rs.getString("FuelType"));
        c.setMileage(rs.getInt("Mileage"));
        c.setDailyRate(rs.getBigDecimal("DailyRate"));
        c.setDepositAmount(rs.getBigDecimal("DepositAmount"));
        c.setStatus(rs.getString("Status"));
        c.setImageUrl(rs.getString("ImageUrl"));
        c.setDescription(rs.getString("Description"));
        c.setTypeName(rs.getString("TypeName"));
        c.setSeatCount(rs.getInt("SeatCount"));
        c.setAvailableQuantity(getOptionalInt(rs, "AvailableQuantity", "AVAILABLE".equals(c.getStatus()) ? 1 : 0));
        return c;
    }

    private int getOptionalInt(ResultSet rs, String columnName, int defaultValue) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                int value = rs.getInt(columnName);
                return rs.wasNull() ? defaultValue : value;
            }
        }
        return defaultValue;
    }
}
