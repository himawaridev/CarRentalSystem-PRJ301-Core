package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@WebServlet("/payment/qr")
public class PaymentQrServlet extends HttpServlet {

    private static final int QR_SIZE = 320;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ref = request.getParameter("ref");
        if (ref == null || ref.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing payment reference");
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        PaymentTransaction paymentTransaction = paymentDAO.getTransactionByRef(ref);
        if (paymentTransaction == null || paymentTransaction.getQrPayload() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Payment transaction not found");
            return;
        }

        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(paymentTransaction.getContractId());
        if (contract == null || !canViewPayment(user, session, contract)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(paymentTransaction.getQrPayload(),
                    BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            MatrixToImageWriter.writeToStream(matrix, "PNG", response.getOutputStream());
        } catch (WriterException e) {
            throw new ServletException("Cannot generate payment QR", e);
        }
    }

    private boolean canViewPayment(User user, HttpSession session, Contract contract) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute("userRoles");
        if (roles != null && (roles.contains("STAFF") || roles.contains("MANAGER") || roles.contains("ADMIN"))) {
            return true;
        }

        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());
        return customerId != null && customerId == contract.getCustomerId();
    }
}
