package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.model.Contract;
import com.carrental.model.Refund;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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
        for (Contract contract : contracts) {
            Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contract.getContractId());
            if (pendingRefund != null) {
                pendingRefundContractIds.add(contract.getContractId());
            }
        }
        request.setAttribute("pendingRefundContractIds", pendingRefundContractIds);

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
}
