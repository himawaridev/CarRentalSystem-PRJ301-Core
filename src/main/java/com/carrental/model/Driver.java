package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maps to dbo.Drivers table.
 */
public class Driver {
    private int driverId;
    private int userId;
    private String licenseNumber;
    private String licenseClass;
    private LocalDate licenseExpiryDate;
    private BigDecimal baseDailyFee;
    private String employmentStatus;
    private LocalDateTime createdAt;

    // Joined
    private String fullName;
    private String phone;

    public Driver() {}

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLicenseClass() { return licenseClass; }
    public void setLicenseClass(String licenseClass) { this.licenseClass = licenseClass; }

    public LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }

    public BigDecimal getBaseDailyFee() { return baseDailyFee; }
    public void setBaseDailyFee(BigDecimal baseDailyFee) { this.baseDailyFee = baseDailyFee; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Availability status (transient, set by DAO query)
    private boolean busy;
    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }
}
