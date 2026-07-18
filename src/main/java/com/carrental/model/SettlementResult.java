package com.carrental.model;

import java.math.BigDecimal;

public class SettlementResult {
    private long contractId;
    private BigDecimal depositPaid = BigDecimal.ZERO;
    private BigDecimal rentalPaid = BigDecimal.ZERO;
    private BigDecimal expectedRental = BigDecimal.ZERO;
    private BigDecimal amountToCollect = BigDecimal.ZERO;

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal depositPaid) { this.depositPaid = safe(depositPaid); }

    public BigDecimal getRentalPaid() { return rentalPaid; }
    public void setRentalPaid(BigDecimal rentalPaid) { this.rentalPaid = safe(rentalPaid); }

    public BigDecimal getExpectedRental() { return expectedRental; }
    public void setExpectedRental(BigDecimal expectedRental) { this.expectedRental = safe(expectedRental); }

    public BigDecimal getAmountToCollect() { return amountToCollect; }
    public void setAmountToCollect(BigDecimal amountToCollect) { this.amountToCollect = safe(amountToCollect); }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
