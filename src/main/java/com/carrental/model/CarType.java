package com.carrental.model;

/**
 * Maps to dbo.Car_Types table.
 */
public class CarType {
    private int carTypeId;
    private String typeName;
    private int seatCount;
    private String description;

    public CarType() {}

    public int getCarTypeId() { return carTypeId; }
    public void setCarTypeId(int carTypeId) { this.carTypeId = carTypeId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "CarType{carTypeId=" + carTypeId + ", typeName='" + typeName + "', seatCount=" + seatCount + "}";
    }
}
