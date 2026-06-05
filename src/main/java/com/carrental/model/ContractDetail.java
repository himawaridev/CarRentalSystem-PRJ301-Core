package com.carrental.model;

import java.math.BigDecimal;

/**
 * Maps to dbo.Contract_Details table.
 */
public class ContractDetail {
    private long contractDetailId;
    private long contractId;
    private int carId;
    private boolean requiresDriver;
    private BigDecimal rentalDailyRate;
    private BigDecimal driverDailyRate;
    private BigDecimal estimatedDays;
    private BigDecimal rentalAmount;
    private BigDecimal driverAmount;
    private BigDecimal lineTotal;
    private String detailStatus;
    private Integer pickupOdometer;
    private Integer returnOdometer;
    private String notes;

    // Joined fields for display
    private String carBrand;
    private String carModel;
    private String licensePlate;
    private int seatCount;
    private String driverName;

    public ContractDetail() {}

    // Getters and setters
    public long getContractDetailId() { return contractDetailId; }
    public void setContractDetailId(long contractDetailId) { this.contractDetailId = contractDetailId; }

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public int getCarId() { return carId; }
    public void setCarId(int carId) { this.carId = carId; }

    public boolean isRequiresDriver() { return requiresDriver; }
    public void setRequiresDriver(boolean requiresDriver) { this.requiresDriver = requiresDriver; }

    public BigDecimal getRentalDailyRate() { return rentalDailyRate; }
    public void setRentalDailyRate(BigDecimal rentalDailyRate) { this.rentalDailyRate = rentalDailyRate; }

    public BigDecimal getDriverDailyRate() { return driverDailyRate; }
    public void setDriverDailyRate(BigDecimal driverDailyRate) { this.driverDailyRate = driverDailyRate; }

    public BigDecimal getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(BigDecimal estimatedDays) { this.estimatedDays = estimatedDays; }

    public BigDecimal getRentalAmount() { return rentalAmount; }
    public void setRentalAmount(BigDecimal rentalAmount) { this.rentalAmount = rentalAmount; }

    public BigDecimal getDriverAmount() { return driverAmount; }
    public void setDriverAmount(BigDecimal driverAmount) { this.driverAmount = driverAmount; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public String getDetailStatus() { return detailStatus; }
    public void setDetailStatus(String detailStatus) { this.detailStatus = detailStatus; }

    public Integer getPickupOdometer() { return pickupOdometer; }
    public void setPickupOdometer(Integer pickupOdometer) { this.pickupOdometer = pickupOdometer; }

    public Integer getReturnOdometer() { return returnOdometer; }
    public void setReturnOdometer(Integer returnOdometer) { this.returnOdometer = returnOdometer; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCarBrand() { return carBrand; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    @Override
    public String toString() {
        return "ContractDetail{contractDetailId=" + contractDetailId + ", carId=" + carId
                + ", lineTotal=" + lineTotal + "}";
    }
}
