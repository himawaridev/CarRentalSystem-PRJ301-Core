package com.carrental.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class RentalPeriodValidator {

    private RentalPeriodValidator() {
    }

    public static boolean isPickupInPast(LocalDateTime pickupAt) {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return pickupAt == null || pickupAt.isBefore(currentMinute);
    }

    public static boolean isValid(LocalDateTime pickupAt, LocalDateTime returnAt) {
        return !isPickupInPast(pickupAt)
                && returnAt != null
                && returnAt.isAfter(pickupAt);
    }
}
