package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/staff/process")
public class ContractProcessServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        String contractIdStr = request.getParameter("contractId");
        String action = request.getParameter("action");

        if (contractIdStr == null || action == null) {
            session.setAttribute("flashError", "Thiếu thông tin!");
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        long contractId = Long.parseLong(contractIdStr);
        ContractDAO dao = new ContractDAO();
        String newStatus;

        switch (action) {
            case "accept" -> newStatus = "ACCEPTED";
            case "reject" -> newStatus = "REJECTED";
            case "deposit_paid" -> newStatus = "DEPOSIT_PAID";
            case "car_picked_up" -> newStatus = "CAR_PICKED_UP";
            case "car_returned" -> newStatus = "CAR_RETURNED";
            case "final_payment" -> newStatus = "FINAL_PAYMENT_COMPLETED";
            case "cancel" -> newStatus = "CANCELLED";
            default -> {
                session.setAttribute("flashError", "Hành động không hợp lệ!");
                response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                return;
            }
        }

        boolean ok = dao.updateContractStatus(contractId, newStatus, user.getUserId());
        if (ok) {
            session.setAttribute("flashSuccess", "Cập nhật hợp đồng #" + contractId + " thành công!");
        } else {
            session.setAttribute("flashError", "Cập nhật thất bại!");
        }

        response.sendRedirect(request.getContextPath() + "/staff/dashboard");
    }
}
