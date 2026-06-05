package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.Car;
import com.carrental.model.CarType;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {

    public List<Car> findAvailableCars(Integer seatCount, LocalDateTime pickupAt, LocalDateTime returnAt) {
        List<Car> cars = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, ct.TypeName, ct.SeatCount FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID "
            + "WHERE c.Status = N'AVAILABLE' ");
        if (seatCount != null) sql.append("AND ct.SeatCount = ? ");
        sql.append("AND NOT EXISTS (SELECT 1 FROM dbo.Contract_Details cd "
            + "INNER JOIN dbo.Contracts con ON cd.ContractID = con.ContractID "
            + "WHERE cd.CarID = c.CarID AND cd.DetailStatus <> N'CANCELLED' "
            + "AND con.Status IN (N'PENDING_REVIEW',N'ACCEPTED',N'DEPOSIT_PAID',N'CAR_PICKED_UP') "
            + "AND con.PickupAt < ? AND con.ReturnAt > ?) ");
        sql.append("AND NOT EXISTS (SELECT 1 FROM dbo.Car_Maintenance cm "
            + "WHERE cm.CarID = c.CarID AND cm.Status IN (N'SCHEDULED',N'IN_PROGRESS') "
            + "AND cm.StartAt < ? AND cm.EndAt > ?) ");
        sql.append("ORDER BY c.DailyRate ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (seatCount != null) ps.setInt(idx++, seatCount);
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
        String sql = "SELECT c.*, ct.TypeName, ct.SeatCount FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID WHERE c.CarID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCar(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT c.*, ct.TypeName, ct.SeatCount FROM dbo.Cars c "
            + "INNER JOIN dbo.Car_Types ct ON c.CarTypeID = ct.CarTypeID ORDER BY c.CarID DESC";
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
        return c;
    }
}
