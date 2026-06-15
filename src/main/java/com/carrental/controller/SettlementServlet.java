package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import com.carrental.model.User;
import com.carrental.model.CheckoutResult;
import com.carrental.service.CheckoutService;
import com.carrental.service.RefundQrBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@WebServlet("/staff/settlement")
public class SettlementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long contractId = parseContractId(request);
        if (contractId <= 0) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(contractId);
        if (contract == null) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
        Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contractId);
        PaymentTransaction balancePaymentTransaction =
                paymentDAO.getLatestPendingBalanceTransactionByContractId(contractId);
        User customerUser = new UserDAO().getUserByCustomerId(contract.getCustomerId());
        String refundQrUrl = null;
        if (pendingRefund != null && customerUser != null && customerUser.hasRefundBankInfo()) {
            refundQrUrl = new RefundQrBuilder().buildRefundQrUrl(
                    customerUser,
                    pendingRefund.getRefundAmount(),
                    "HOAN " + contract.getContractCode() + " RF" + pendingRefund.getRefundId());
        }

        request.setAttribute("contract", contract);
        request.setAttribute("settlement", settlement);
        request.setAttribute("pendingRefund", pendingRefund);
        request.setAttribute("balancePaymentTransaction", balancePaymentTransaction);
        request.setAttribute("paymentRecords", paymentDAO.getPaymentRecordsByContractId(contractId));
        request.setAttribute("customerUser", customerUser);
        request.setAttribute("refundQrUrl", refundQrUrl);

        HttpSession session = request.getSession(false);
        if (session != null) {
            String flash = (String) session.getAttribute("flashSuccess");
            if (flash != null) {
                request.setAttribute("success", flash);
                session.removeAttribute("flashSuccess");
            }
            String err = (String) session.getAttribute("flashError");
            if (err != null) {
                request.setAttribute("error", err);
                session.removeAttribute("flashError");
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/settlement.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");
        long contractId = parseContractId(request);
        String action = request.getParameter("action");

        if (contractId <= 0 || action == null) {
            session.setAttribute("flashError", "Thieu thong tin quyet toan.");
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        if ("createBalancePayment".equals(action)) {
            SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
            BigDecimal amountToCollect = settlement.getAmountToCollect();
            if (amountToCollect.compareTo(BigDecimal.ZERO) <= 0) {
                session.setAttribute("flashError", "Khong con so tien can thu them.");
            } else {
                Contract contract = new ContractDAO().getContractById(contractId);
                PaymentTransaction tx = contract == null ? null : paymentDAO.createRentalBalancePaymentLink(
                        contractId,
                        contract.getContractCode(),
                        amountToCollect);
                session.setAttribute(tx != null ? "flashSuccess" : "flashError",
                        tx != null
                                ? "Da tao QR thu tien thue/tai xe con lai. Cho khach quet QR roi bam kiem tra giao dich."
                                : "Khong the tao QR thu tien con lai.");
            }
        } else if ("checkBalancePayment".equals(action)) {
            String paymentRef = request.getParameter("balancePaymentRef");
            PaymentTransaction tx = null;
            if (paymentRef != null && !paymentRef.isBlank()) {
                tx = paymentDAO.reconcilePendingTransactionWithGateway(paymentRef.trim());
            } else {
                tx = paymentDAO.getLatestPendingBalanceTransactionByContractId(contractId);
                if (tx != null) {
                    tx = paymentDAO.reconcilePendingTransactionWithGateway(tx.getProviderTransactionRef());
                }
            }

            if (tx == null) {
                session.setAttribute("flashError", "Chua co QR thu tien con lai de kiem tra.");
            } else if (tx.getStatus() == PaymentStatus.PAID) {
                session.setAttribute("flashSuccess",
                        "Khach da thanh toan tien thue/tai xe con lai. Bay gio co the tao QR hoan coc thu cong.");
            } else if (tx.getExpiredAt() != null && tx.getExpiredAt().isBefore(LocalDateTime.now())) {
                session.setAttribute("flashError", "QR thu tien con lai da het han. Hay tao QR moi.");
            } else {
                session.setAttribute("flashError",
                        "PayOS chua xac nhan thanh toan. Trang thai hien tai: " + tx.getStatus());
            }
        } else if ("createRefund".equals(action)) {
            SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
            if (settlement.getAmountToCollect().compareTo(BigDecimal.ZERO) > 0) {
                session.setAttribute("flashError",
                        "Can thu du tien thue/tai xe con lai truoc khi tao QR hoan coc.");
                response.sendRedirect(request.getContextPath() + "/staff/settlement?contractId=" + contractId);
                return;
            }
            UserDAO userDAO = new UserDAO();
            ContractDAO contractDAO = new ContractDAO();
            Contract contract = contractDAO.getContractById(contractId);
            User customerUser = contract == null ? null : userDAO.getUserByCustomerId(contract.getCustomerId());
            if (customerUser == null || !customerUser.hasRefundBankInfo()) {
                session.setAttribute("flashError",
                        "Khach hang chua co tai khoan ngan hang nhan hoan tien. Hay yeu cau khach cap nhat profile.");
            } else {
                Refund refund = paymentDAO.createRefundRequest(
                    contractId,
                    user.getUserId(),
                    request.getParameter("reason"),
                    RefundMethod.MANUAL_BANK_TRANSFER);
                session.setAttribute(refund != null ? "flashSuccess" : "flashError",
                        refund != null
                                ? "Da tao QR hoan tien thu cong. Hay quet QR, chuyen khoan va nhap ma giao dich."
                                : "Khong the tao yeu cau hoan tien.");
            }
        } else if ("processCheckout".equals(action)) {
            CheckoutService checkoutService = new CheckoutService();
            CheckoutResult result = checkoutService.processCheckout(
                    contractId,
                    user.getUserId(),
                    parseRefundMethod(request.getParameter("refundMethod")),
                    request.getParameter("proofOfRefund"),
                    request.getParameter("reason"));
            session.setAttribute(result.isSuccess() ? "flashSuccess" : "flashError", result.getMessage());
        } else if ("completeRefund".equals(action)) {
            long refundId = parseLong(request.getParameter("refundId"));
            String providerRefundRef = request.getParameter("providerRefundRef");
            String proofOfRefund = request.getParameter("proofOfRefund");
            RefundMethod refundMethod = parseRefundMethod(request.getParameter("refundMethod"));
            if (refundMethod == RefundMethod.MANUAL_BANK_TRANSFER
                    && (proofOfRefund == null || proofOfRefund.isBlank())) {
                session.setAttribute("flashError", "Can nhap ma giao dich khi hoan coc bang chuyen khoan thu cong.");
                response.sendRedirect(request.getContextPath() + "/staff/settlement?contractId=" + contractId);
                return;
            }
            if ((providerRefundRef == null || providerRefundRef.isBlank()) && proofOfRefund != null) {
                providerRefundRef = proofOfRefund.trim();
            }

            boolean ok = refundId > 0 && paymentDAO.markRefundCompleted(
                    refundId, refundMethod, providerRefundRef, proofOfRefund, user.getUserId());
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? "Da xac nhan hoan coc va hoan tat hop dong."
                       : "Khong the xac nhan hoan coc.");
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                return;
            }
        } else if ("complete".equals(action)) {
            SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
            Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contractId);
            if (pendingRefund == null
                    && settlement.getAmountToCollect().compareTo(BigDecimal.ZERO) == 0
                    && settlement.getRefundAmount().compareTo(BigDecimal.ZERO) == 0) {
                ContractDAO contractDAO = new ContractDAO();
                boolean ok = contractDAO.updateContractStatus(contractId, ContractStatus.COMPLETED, user.getUserId());
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                        ok ? "Hop dong da hoan tat." : "Khong the hoan tat hop dong.");
            } else {
                session.setAttribute("flashError", "Van con tien can thu hoac tien coc can hoan.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/staff/settlement?contractId=" + contractId);
    }

    private long parseContractId(HttpServletRequest request) {
        return parseLong(request.getParameter("contractId"));
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    private RefundMethod parseRefundMethod(String value) {
        try {
            return value == null || value.isBlank()
                    ? RefundMethod.MANUAL_BANK_TRANSFER
                    : RefundMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            return RefundMethod.MANUAL_BANK_TRANSFER;
        }
    }
}
