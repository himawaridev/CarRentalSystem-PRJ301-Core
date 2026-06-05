package com.carrental.model;

import java.time.LocalDateTime;

/**
 * Maps to dbo.Driver_Assignments table.
 */
public class DriverAssignment {
    private long assignmentId;
    private long contractDetailId;
    private int driverId;
    private Integer assignedByUserId;
    private LocalDateTime assignedAt;
    private String assignmentStatus;
    private LocalDateTime handoverReceivedAt;
    private LocalDateTime tripCompletedAt;
    private String driverNote;

    // Joined fields for display
    private String carBrand;
    private String carModel;
    private String licensePlate;
    private String customerName;
    private LocalDateTime pickupAt;
    private LocalDateTime returnAt;
    private String pickupLocation;
    private String returnLocation;
    private String contractCode;

    public DriverAssignment() {}

    public long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(long assignmentId) { this.assignmentId = assignmentId; }

    public long getContractDetailId() { return contractDetailId; }
    public void setContractDetailId(long contractDetailId) { this.contractDetailId = contractDetailId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public Integer getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(Integer assignedByUserId) { this.assignedByUserId = assignedByUserId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getAssignmentStatus() { return assignmentStatus; }
    public void setAssignmentStatus(String assignmentStatus) { this.assignmentStatus = assignmentStatus; }

    public LocalDateTime getHandoverReceivedAt() { return handoverReceivedAt; }
    public void setHandoverReceivedAt(LocalDateTime handoverReceivedAt) { this.handoverReceivedAt = handoverReceivedAt; }

    public LocalDateTime getTripCompletedAt() { return tripCompletedAt; }
    public void setTripCompletedAt(LocalDateTime tripCompletedAt) { this.tripCompletedAt = tripCompletedAt; }

    public String getDriverNote() { return driverNote; }
    public void setDriverNote(String driverNote) { this.driverNote = driverNote; }

    public String getCarBrand() { return carBrand; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getPickupAt() { return pickupAt; }
    public void setPickupAt(LocalDateTime pickupAt) { this.pickupAt = pickupAt; }

    public LocalDateTime getReturnAt() { return returnAt; }
    public void setReturnAt(LocalDateTime returnAt) { this.returnAt = returnAt; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getReturnLocation() { return returnLocation; }
    public void setReturnLocation(String returnLocation) { this.returnLocation = returnLocation; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }
}
