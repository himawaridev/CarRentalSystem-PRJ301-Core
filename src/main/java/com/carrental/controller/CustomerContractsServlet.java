package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/my-contracts")
public class CustomerContractsServlet extends HttpServlet {

    // Statuses where customer can cancel (before payment)
    private static final Set<String> CANCELLABLE = Set.of(
        ContractStatus.PENDING_PAYMENT
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
            for (Contract contract : contracts) {
                if (ContractStatus.PENDING_PAYMENT.equals(contract.getStatus())) {
                    PaymentTransaction tx = paymentDAO.getLatestPendingTransactionByContractId(contract.getContractId());
                    if (tx != null) {
                        pendingPaymentRefs.put(contract.getContractId(), tx.getProviderTransactionRef());
                    }
                }
            }
            request.setAttribute("pendingPaymentRefs", pendingPaymentRefs);
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
            } else if (!CANCELLABLE.contains(contract.getStatus())) {
                session.setAttribute("flashError",
                    "Khong the huy hop dong o trang thai \"" + contract.getStatus()
                    + "\". Chi co the huy khi chua nhan xe.");
            } else {
                boolean ok = contractDAO.updateContractStatus(contractId, ContractStatus.CANCELLED, user.getUserId());
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? "Da huy hop dong " + contract.getContractCode() + " thanh cong!"
                       : "Huy hop dong that bai! Vui long thu lai.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/my-contracts");
    }
}
