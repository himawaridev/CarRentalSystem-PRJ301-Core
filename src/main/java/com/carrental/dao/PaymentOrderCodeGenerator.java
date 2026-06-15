package com.carrental.dao;

import java.util.concurrent.ThreadLocalRandom;

final class PaymentOrderCodeGenerator {
    private PaymentOrderCodeGenerator() {
    }

    static long generate(long contractId) {
        long entropy = System.currentTimeMillis() % 100_000L;
        long random = ThreadLocalRandom.current().nextLong(100L, 999L);
        return (contractId * 100_000_000L) + (entropy * 1_000L) + random;
    }
}
