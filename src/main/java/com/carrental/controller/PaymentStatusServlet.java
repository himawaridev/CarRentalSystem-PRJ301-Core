package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import org.json.JSONObject;

@WebServlet("/payment/status")
public class PaymentStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        String ref = request.getParameter("ref");
        if (ref == null || ref.isBlank()) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    new JSONObject().put("ok", false).put("message", "Missing payment reference."));
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (user == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    new JSONObject().put("ok", false).put("message", "Authentication required."));
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        PaymentTransaction tx = paymentDAO.getTransactionByRef(ref);
        if (tx == null) {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                    new JSONObject().put("ok", false).put("message", "Payment transaction not found."));
            return;
        }

        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(tx.getContractId());
        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());
        if (contract == null || customerId == null || contract.getCustomerId() != customerId) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                    new JSONObject().put("ok", false).put("message", "Access denied."));
            return;
        }

        if (tx.getStatus() == PaymentStatus.PENDING) {
            tx = paymentDAO.reconcilePendingTransactionWithGateway(ref);
            if (tx == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        new JSONObject().put("ok", false).put("message", "Payment transaction not found."));
                return;
            }
            contract = contractDAO.getContractById(tx.getContractId());
        }

        boolean paid = tx.getStatus() == PaymentStatus.PAID;
        boolean expired = tx.getStatus() == PaymentStatus.PENDING
                && tx.getExpiredAt() != null
                && tx.getExpiredAt().isBefore(LocalDateTime.now());

        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("paymentStatus", tx.getStatus().name());
        json.put("contractStatus", contract.getStatus());
        json.put("paid", paid);
        json.put("expired", expired);
        json.put("redirectUrl", request.getContextPath() + "/my-contracts");
        if (tx.getPaidAt() != null) {
            json.put("paidAt", tx.getPaidAt().toString());
        }

        writeJson(response, HttpServletResponse.SC_OK, json);
    }

    private void writeJson(HttpServletResponse response, int status, JSONObject json) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json.toString());
    }
}
