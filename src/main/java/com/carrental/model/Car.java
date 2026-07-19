package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to dbo.Cars table. Includes joined CarType info for display.
 */
public class Car {
    private int carId;
    private int carTypeId;
    private String licensePlate;
    private String brand;
    private String model;
    private Short manufactureYear;
    private String color;
    private String transmission;
    private String fuelType;
    private int mileage;
    private BigDecimal dailyRate;
    private BigDecimal depositAmount;
    private String status;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;

    // Joined fields from Car_Types
    private String typeName;
    private int seatCount;
    private int availableQuantity;
    private boolean booked;

    public Car() {}

    // Getters and setters
    public int getCarId() { return carId; }
    public void setCarId(int carId) { this.carId = carId; }

    public int getCarTypeId() { return carTypeId; }
    public void setCarTypeId(int carTypeId) { this.carTypeId = carTypeId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Short getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(Short manufactureYear) { this.manufactureYear = manufactureYear; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = Math.max(availableQuantity, 0); }

    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public String getDisplayStatus() {
        return booked ? "BOOKED" : status;
    }

    @Override
    public String toString() {
        return "Car{carId=" + carId + ", brand='" + brand + "', model='" + model
                + "', dailyRate=" + dailyRate + "}";
    }
}
