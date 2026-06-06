package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.DriverDAO;
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

        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/my-contracts");
            return;
        }

        long contractId = Long.parseLong(idStr);
        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(contractId);

        // Verify ownership
        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());

        if (contract == null || customerId == null || contract.getCustomerId() != customerId) {
            response.sendRedirect(request.getContextPath() + "/my-contracts");
            return;
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

        request.getRequestDispatcher("/WEB-INF/views/contract-detail.jsp").forward(request, response);
    }
}
