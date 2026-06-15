package com.carrental.dao;

import com.carrental.service.PayOsGateway;
import com.carrental.service.PaymentLinkRequest;
import com.carrental.service.PaymentLinkResponse;
import java.io.IOException;
import java.sql.SQLException;

final class PaymentGatewayLinkService {
    private PaymentGatewayLinkService() {
    }

    static PaymentLinkResponse createPaymentLink(PaymentLinkRequest request) throws SQLException {
        PayOsGateway gateway = new PayOsGateway();
        try {
            return gateway.createPaymentLink(request);
        } catch (IOException | RuntimeException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new SQLException("Cannot create real payment link from payOS: " + e.getMessage(), e);
        }
    }
}
