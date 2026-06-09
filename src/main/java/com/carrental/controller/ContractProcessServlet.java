package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.model.ContractStatus;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
            session.setAttribute("flashError", "Thieu thong tin.");
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        long contractId = Long.parseLong(contractIdStr);
        String newStatus;

        switch (action) {
            case "confirm", "accept" -> newStatus = ContractStatus.CONFIRMED;
            case "car_picked_up" -> newStatus = ContractStatus.CAR_PICKED_UP;
            case "car_returned" -> newStatus = ContractStatus.CAR_RETURNED;
            case "settlement_pending" -> newStatus = ContractStatus.SETTLEMENT_PENDING;
            case "complete", "final_payment" -> newStatus = ContractStatus.COMPLETED;
            case "cancel" -> newStatus = ContractStatus.CANCELLED;
            default -> {
                session.setAttribute("flashError", "Hanh dong khong hop le.");
                response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                return;
            }
        }

        ContractDAO dao = new ContractDAO();
        boolean ok = dao.updateContractStatus(contractId, newStatus, user.getUserId());
        session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Cap nhat hop dong #" + contractId + " thanh cong."
                   : "Cap nhat that bai. Vui long kiem tra trang thai hien tai.");

        response.sendRedirect(request.getContextPath() + "/staff/dashboard");
    }
}
