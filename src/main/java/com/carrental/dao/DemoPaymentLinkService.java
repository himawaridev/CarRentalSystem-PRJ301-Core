package com.carrental.dao;

import com.carrental.service.PaymentLinkRequest;
import com.carrental.service.PaymentLinkResponse;
import java.sql.SQLException;

final class DemoPaymentLinkService {
    private static final String DEMO_PROVIDER = "DEMO";

    private DemoPaymentLinkService() {
    }

    static PaymentLinkResponse createPaymentLink(PaymentLinkRequest request) throws SQLException {
        if (request == null || request.getAmount() == null) {
            throw new SQLException("Payment request is invalid.");
        }

        PaymentLinkResponse response = new PaymentLinkResponse();
        response.setProvider(DEMO_PROVIDER);
        response.setPaymentLinkId("DEMO-" + request.getOrderCode());
        response.setCheckoutUrl(null);
        response.setQrCode(null);
        response.setRawResponse("{\"provider\":\"DEMO\",\"message\":\"Internal demo payment\"}");
        return response;
    }
}
