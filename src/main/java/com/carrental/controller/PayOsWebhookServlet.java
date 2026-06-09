package com.carrental.controller;

import com.carrental.dao.PaymentDAO;
import com.carrental.model.PaymentWebhookResult;
import com.carrental.service.PayOsGateway;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import org.json.JSONObject;

@WebServlet("/payment/webhook/payos")
public class PayOsWebhookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String body = readBody(request);
        JSONObject payload;
        try {
            payload = new JSONObject(body);
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_json", "Invalid JSON payload.");
            return;
        }

        JSONObject data = payload.optJSONObject("data");
        String signature = payload.optString("signature", "");
        if (data == null || signature.isBlank()) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "bad_request", "Missing data or signature.");
            return;
        }

        PayOsGateway gateway = new PayOsGateway();
        if (!gateway.verifyWebhook(data, signature)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_signature", "Invalid signature.");
            return;
        }

        boolean paidSuccessfully = payload.optBoolean("success")
                && "00".equals(payload.optString("code"))
                && "00".equals(data.optString("code"));
        if (!paidSuccessfully) {
            writeJson(response, HttpServletResponse.SC_OK, "ignored", "Webhook verified but payment is not successful.");
            return;
        }

        long orderCode = data.optLong("orderCode", -1L);
        BigDecimal amount = BigDecimal.valueOf(data.optLong("amount", -1L));
        String reference = data.optString("reference", "");
        if (orderCode <= 0 || amount.compareTo(BigDecimal.ZERO) <= 0 || reference.isBlank()) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "bad_request", "Missing payment reference fields.");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        PaymentWebhookResult result = paymentDAO.confirmPaymentFromGatewayWebhook(
                PayOsGateway.PROVIDER,
                orderCode,
                amount,
                reference,
                body,
                signature);

        if (result.isSuccess()) {
            writeJson(response, HttpServletResponse.SC_OK,
                    result.isDuplicate() ? "duplicate" : "ok", result.getMessage());
        } else {
            writeJson(response, HttpServletResponse.SC_CONFLICT, "failed", result.getMessage());
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[2048];
        int read;
        while ((read = request.getReader().read(buffer)) != -1) {
            body.append(buffer, 0, read);
        }
        return body.toString();
    }

    private void writeJson(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("message", message);
        response.getWriter().write(json.toString());
    }
}
