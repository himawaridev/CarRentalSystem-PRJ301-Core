package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.DriverDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/contract-detail")
public class ContractDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute("userRoles");
        boolean staffView = roles != null
                && (roles.contains("STAFF") || roles.contains("MANAGER") || roles.contains("ADMIN"));

        String idStr = request.getParameter("id");
        if (idStr == null) {
            redirectToContractList(request, response, staffView);
            return;
        }

        long contractId;
        try {
            contractId = Long.parseLong(idStr);
            if (contractId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            redirectToContractList(request, response, staffView);
            return;
        }
        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(contractId);

        if (contract == null) {
            redirectToContractList(request, response, staffView);
            return;
        }

        if (!staffView) {
            Integer customerId = new UserDAO().getCustomerIdByUserId(user.getUserId());
            if (customerId == null || contract.getCustomerId() != customerId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
        }

        // Get details with car info
        List<ContractDetail> details = contractDAO.getDetailsByContractId(contractId);

        // Get driver assignments for each detail
        DriverDAO driverDAO = new DriverDAO();
        java.util.Map<Long, DriverAssignment> driverAssignments = new java.util.HashMap<>();
        for (ContractDetail d : details) {
            DriverAssignment a = driverDAO.getAssignmentByDetailId(d.getContractDetailId());
            if (a != null) {
                driverAssignments.put(d.getContractDetailId(), a);
            }
        }

        request.setAttribute("contract", contract);
        request.setAttribute("details", details);
        request.setAttribute("driverAssignments", driverAssignments);
        request.setAttribute("staffView", staffView);

        PaymentDAO paymentDAO = new PaymentDAO();
        request.setAttribute("paymentRecords", paymentDAO.getPaymentRecordsByContractId(contractId));

        request.getRequestDispatcher("/WEB-INF/views/contract-detail.jsp").forward(request, response);
    }

    private void redirectToContractList(HttpServletRequest request, HttpServletResponse response,
            boolean staffView) throws IOException {
        response.sendRedirect(request.getContextPath()
                + (staffView ? "/staff/dashboard" : "/my-contracts"));
    }
}
