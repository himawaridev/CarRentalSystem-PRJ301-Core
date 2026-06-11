package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.Refund;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/my-contracts")
public class CustomerContractsServlet extends HttpServlet {

    private static final long FREE_CANCEL_HOURS = 48;
    private static final Set<String> PAID_CANCELLABLE = Set.of(
            ContractStatus.RESERVED,
            ContractStatus.CONFIRMED
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");

        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());

        if (customerId != null) {
            ContractDAO contractDAO = new ContractDAO();
            List<Contract> contracts = contractDAO.getContractsByCustomerId(customerId);
            request.setAttribute("contracts", contracts);

            PaymentDAO paymentDAO = new PaymentDAO();
            Map<Long, String> pendingPaymentRefs = new HashMap<>();
            Set<Long> cancellableContractIds = new HashSet<>();
            Set<Long> pendingRefundContractIds = new HashSet<>();
            Map<Long, BigDecimal> cancellationRefundAmounts = new HashMap<>();
            for (Contract contract : contracts) {
                if (ContractStatus.PENDING_PAYMENT.equals(contract.getStatus())) {
                    PaymentTransaction tx = paymentDAO.getLatestPendingTransactionByContractId(contract.getContractId());
                    if (tx != null) {
                        pendingPaymentRefs.put(contract.getContractId(), tx.getProviderTransactionRef());
                    }
                }
                if (canCustomerCancel(contract)) {
                    cancellableContractIds.add(contract.getContractId());
                }

                Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contract.getContractId());
                if (pendingRefund != null) {
                    pendingRefundContractIds.add(contract.getContractId());
                } else if (PAID_CANCELLABLE.contains(contract.getStatus())) {
                    BigDecimal refundAmount = paymentDAO.calculateCancellationRefundAmount(contract.getContractId());
                    if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                        cancellationRefundAmounts.put(contract.getContractId(), refundAmount);
                    }
                }
            }
            request.setAttribute("pendingPaymentRefs", pendingPaymentRefs);
            request.setAttribute("cancellableContractIds", cancellableContractIds);
            request.setAttribute("pendingRefundContractIds", pendingRefundContractIds);
            request.setAttribute("cancellationRefundAmounts", cancellationRefundAmounts);
        }

        // Flash messages
        String flash = (String) session.getAttribute("flashSuccess");
        if (flash != null) { request.setAttribute("success", flash); session.removeAttribute("flashSuccess"); }
        String err = (String) session.getAttribute("flashError");
        if (err != null) { request.setAttribute("error", err); session.removeAttribute("flashError"); }

        request.getRequestDispatcher("/WEB-INF/views/my-contracts.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");
        String action = request.getParameter("action");

        if ("cancel".equals(action)) {
            long contractId = Long.parseLong(request.getParameter("contractId"));

            // Verify this contract belongs to the logged-in customer
            ContractDAO contractDAO = new ContractDAO();
            Contract contract = contractDAO.getContractById(contractId);

            UserDAO userDAO = new UserDAO();
            Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());

            if (contract == null || customerId == null || contract.getCustomerId() != customerId) {
                session.setAttribute("flashError", "Khong tim thay hop dong hoac ban khong co quyen huy!");
            } else if (!canCustomerCancel(contract)) {
                session.setAttribute("flashError",
                    "Khong the huy hop dong o trang thai \"" + contract.getStatus()
                    + "\". Don da thanh toan chi duoc huy/hoan tien khi con truoc gio nhan xe it nhat 2 ngay.");
            } else {
                if (!ContractStatus.PENDING_PAYMENT.equals(contract.getStatus())) {
                    User currentUser = userDAO.getUserById(user.getUserId());
                    if (currentUser == null || !currentUser.hasRefundBankInfo()) {
                        session.setAttribute("flashError",
                                "Vui long cap nhat tai khoan ngan hang truoc khi huy don da thanh toan.");
                        response.sendRedirect(request.getContextPath() + "/profile?required=bank&redirect="
                                + java.net.URLEncoder.encode(request.getContextPath() + "/my-contracts", "UTF-8"));
                        return;
                    }
                }

                PaymentDAO paymentDAO = new PaymentDAO();
                boolean ok = paymentDAO.cancelContractWithRefund(
                        contractId,
                        user.getUserId(),
                        "Khach huy hop dong truoc " + FREE_CANCEL_HOURS + " gio nhan xe.");
                Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contractId);
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? (pendingRefund == null
                            ? "Da huy hop dong " + contract.getContractCode() + " thanh cong!"
                            : "Da huy hop dong " + contract.getContractCode()
                                    + ". Yeu cau hoan tien da duoc tao va dang cho nhan vien xu ly.")
                       : "Huy hop dong that bai! Vui long thu lai.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/my-contracts");
    }

    private boolean canCustomerCancel(Contract contract) {
        if (contract == null) {
            return false;
        }
        if (ContractStatus.PENDING_PAYMENT.equals(contract.getStatus())) {
            return true;
        }
        if (!PAID_CANCELLABLE.contains(contract.getStatus()) || contract.getPickupAt() == null) {
            return false;
        }
        return ChronoUnit.HOURS.between(LocalDateTime.now(), contract.getPickupAt()) >= FREE_CANCEL_HOURS;
    }
}
