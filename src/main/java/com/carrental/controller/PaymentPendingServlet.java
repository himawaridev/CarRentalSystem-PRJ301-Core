package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/payment/pending")
public class PaymentPendingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ref = request.getParameter("ref");
        if (ref == null || ref.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/my-contracts");
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        PaymentTransaction paymentTransaction = paymentDAO.getTransactionByRef(ref);
        if (paymentTransaction == null) {
            session.setAttribute("flashError", "Khong tim thay giao dich thanh toan.");
            response.sendRedirect(request.getContextPath() + "/my-contracts");
            return;
        }

        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(paymentTransaction.getContractId());
        if (contract == null || !canViewPayment(user, session, contract)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        request.setAttribute("contract", contract);
        request.setAttribute("paymentTransaction", paymentTransaction);
        request.setAttribute("paymentReturnUrl", paymentReturnUrl(request, session, contract));
        request.getRequestDispatcher("/WEB-INF/views/payment-pending.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ref = request.getParameter("ref");
        if (ref == null || ref.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/my-contracts");
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        PaymentTransaction paymentTransaction = paymentDAO.getTransactionByRef(ref);
        if (paymentTransaction == null) {
            session.setAttribute("flashError", "Khong tim thay giao dich thanh toan.");
            response.sendRedirect(request.getContextPath() + "/my-contracts");
            return;
        }

        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(paymentTransaction.getContractId());
        if (contract == null || !canViewPayment(user, session, contract)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String paidRef = "DEMO-PAID-" + paymentTransaction.getProviderTransactionRef();
        boolean paid = paymentDAO.confirmPaymentTransaction(paymentTransaction.getProviderTransactionRef(), paidRef);
        if (paid) {
            session.setAttribute("flashSuccess", "Thanh toan thanh cong. Hop dong da duoc giu xe.");
        } else {
            session.setAttribute("flashError", "Khong the xac nhan thanh toan. Giao dich co the da het han.");
        }

        response.sendRedirect(paymentReturnUrl(request, session, contract));
    }

    private boolean canViewPayment(User user, HttpSession session, Contract contract) {
        if (hasStaffRole(session)) {
            return true;
        }

        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());
        return customerId != null && customerId == contract.getCustomerId();
    }

    private String paymentReturnUrl(HttpServletRequest request, HttpSession session, Contract contract) {
        if (hasStaffRole(session)) {
            return request.getContextPath() + "/staff/settlement?contractId=" + contract.getContractId();
        }
        return request.getContextPath() + "/my-contracts";
    }

    private boolean hasStaffRole(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute("userRoles");
        return roles != null && (roles.contains("STAFF") || roles.contains("MANAGER") || roles.contains("ADMIN"));
    }
}
