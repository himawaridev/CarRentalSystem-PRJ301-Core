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
        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());

        if (contract == null || customerId == null || contract.getCustomerId() != customerId) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        request.setAttribute("contract", contract);
        request.setAttribute("paymentTransaction", paymentTransaction);
        request.getRequestDispatcher("/WEB-INF/views/payment-pending.jsp").forward(request, response);
    }
}
