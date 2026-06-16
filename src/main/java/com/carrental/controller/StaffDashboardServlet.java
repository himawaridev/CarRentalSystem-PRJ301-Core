package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentStatus;
import com.carrental.model.Refund;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/staff/dashboard")
public class StaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ContractDAO contractDAO = new ContractDAO();

        String statusFilter = request.getParameter("status");
        List<Contract> contracts;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            contracts = contractDAO.getContractsByStatus(statusFilter);
        } else {
            contracts = contractDAO.getAllContracts();
        }
        request.setAttribute("contracts", contracts);
        request.setAttribute("statusFilter", statusFilter);

        PaymentDAO paymentDAO = new PaymentDAO();
        Set<Long> pendingRefundContractIds = new HashSet<>();
        Set<Long> refundedContractIds = new HashSet<>();
        Map<Long, BigDecimal> cancellationRefundAmounts = new HashMap<>();
        Map<Long, String> refundedRefundAmountTexts = new HashMap<>();
        for (Contract contract : contracts) {
            Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contract.getContractId());
            if (pendingRefund != null) {
                pendingRefundContractIds.add(contract.getContractId());
            } else {
                Refund latestRefund = paymentDAO.getLatestRefundByContractId(contract.getContractId());
                if (latestRefund != null && latestRefund.getStatus() == PaymentStatus.REFUNDED) {
                    refundedContractIds.add(contract.getContractId());
                    refundedRefundAmountTexts.put(
                            contract.getContractId(), formatMoney(latestRefund.getRefundAmount()));
                }
            }

            if (!pendingRefundContractIds.contains(contract.getContractId())
                    && canStaffCancelWithRefund(contract)) {
                BigDecimal refundAmount = paymentDAO.calculateCancellationRefundAmount(contract.getContractId());
                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                    cancellationRefundAmounts.put(contract.getContractId(), refundAmount);
                }
            }
        }
        request.setAttribute("pendingRefundContractIds", pendingRefundContractIds);
        request.setAttribute("refundedContractIds", refundedContractIds);
        request.setAttribute("cancellationRefundAmounts", cancellationRefundAmounts);
        request.setAttribute("refundedRefundAmountTexts", refundedRefundAmountTexts);

        // Flash messages
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

        request.getRequestDispatcher("/WEB-INF/views/staff-dashboard.jsp").forward(request, response);
    }

    private boolean canStaffCancelWithRefund(Contract contract) {
        return contract != null
                && (ContractStatus.RESERVED.equals(contract.getStatus())
                || ContractStatus.CONFIRMED.equals(contract.getStatus()));
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        return format.format(value == null ? BigDecimal.ZERO : value);
    }
}
