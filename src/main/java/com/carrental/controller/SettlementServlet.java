package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import com.carrental.model.User;
import com.carrental.model.CheckoutResult;
import com.carrental.service.CheckoutService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

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

        request.setAttribute("contract", contract);
        request.setAttribute("settlement", settlement);
        request.setAttribute("pendingRefund", pendingRefund);

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
        if ("collectBalance".equals(action)) {
            SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
            BigDecimal amountToCollect = settlement.getAmountToCollect();
            if (amountToCollect.compareTo(BigDecimal.ZERO) <= 0) {
                session.setAttribute("flashError", "Khong con so tien can thu them.");
            } else {
                boolean ok = paymentDAO.recordRentalBalancePayment(contractId, amountToCollect,
                        user.getUserId(), "Thu tien thue/phat sinh khi quyet toan");
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                        ok ? "Da ghi nhan thu them thanh cong." : "Ghi nhan thu them that bai.");
            }
        } else if ("processCheckout".equals(action) || "createRefund".equals(action)) {
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
                    ? RefundMethod.GATEWAY_REFUND
                    : RefundMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            return RefundMethod.GATEWAY_REFUND;
        }
    }
}
