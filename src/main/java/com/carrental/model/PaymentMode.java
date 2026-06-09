package com.carrental.model;

public enum PaymentMode {
    DEPOSIT_ONLY,
    FULL_PREPAYMENT;

    public static PaymentMode fromRequest(String value) {
        if (FULL_PREPAYMENT.name().equals(value)) {
            return FULL_PREPAYMENT;
        }
        return DEPOSIT_ONLY;
    }
}
