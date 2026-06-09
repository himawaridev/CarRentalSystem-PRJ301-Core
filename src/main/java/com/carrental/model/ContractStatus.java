package com.carrental.model;

public final class ContractStatus {
    public static final String PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String PAYMENT_EXPIRED = "PAYMENT_EXPIRED";
    public static final String RESERVED = "RESERVED";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";
    public static final String CAR_PICKED_UP = "CAR_PICKED_UP";
    public static final String CAR_RETURNED = "CAR_RETURNED";
    public static final String SETTLEMENT_PENDING = "SETTLEMENT_PENDING";
    public static final String COMPLETED = "COMPLETED";

    private ContractStatus() {
    }
}
