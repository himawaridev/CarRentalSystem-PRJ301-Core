package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to dbo.Contracts table.
 */
public class Contract {
    private long contractId;
    private String contractCode;
    private int customerId;
    private LocalDateTime pickupAt;
    private LocalDateTime returnAt;
    private String pickupLocation;
    private String returnLocation;
    private String status;
    private Integer reviewedByUserId;
    private LocalDateTime reviewedAt;
    private String reviewNote;
    private String rejectionReason;
    private BigDecimal depositAmountDue;
    private BigDecimal finalAmountDue;
    private LocalDateTime depositPaidAt;
    private LocalDateTime finalPaidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields for display
    private String customerName;
    private String customerPhone;

    public Contract() {}

    // Getters and setters
    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public LocalDateTime getPickupAt() { return pickupAt; }
    public void setPickupAt(LocalDateTime pickupAt) { this.pickupAt = pickupAt; }

    public LocalDateTime getReturnAt() { return returnAt; }
    public void setReturnAt(LocalDateTime returnAt) { this.returnAt = returnAt; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getReturnLocation() { return returnLocation; }
    public void setReturnLocation(String returnLocation) { this.returnLocation = returnLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Integer reviewedByUserId) { this.reviewedByUserId = reviewedByUserId; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public BigDecimal getDepositAmountDue() { return depositAmountDue; }
    public void setDepositAmountDue(BigDecimal depositAmountDue) { this.depositAmountDue = depositAmountDue; }

    public BigDecimal getFinalAmountDue() { return finalAmountDue; }
    public void setFinalAmountDue(BigDecimal finalAmountDue) { this.finalAmountDue = finalAmountDue; }

    public LocalDateTime getDepositPaidAt() { return depositPaidAt; }
    public void setDepositPaidAt(LocalDateTime depositPaidAt) { this.depositPaidAt = depositPaidAt; }

    public LocalDateTime getFinalPaidAt() { return finalPaidAt; }
    public void setFinalPaidAt(LocalDateTime finalPaidAt) { this.finalPaidAt = finalPaidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    @Override
    public String toString() {
        return "Contract{contractId=" + contractId + ", contractCode='" + contractCode
                + "', status='" + status + "'}";
    }
}
