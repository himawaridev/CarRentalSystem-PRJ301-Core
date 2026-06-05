package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/staff/contract-detail")
public class ContractDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }
        long contractId = Long.parseLong(idStr);
        ContractDAO dao = new ContractDAO();
        Contract contract = dao.getContractById(contractId);
        List<ContractDetail> details = dao.getDetailsByContractId(contractId);

        request.setAttribute("contract", contract);
        request.setAttribute("details", details);
        request.getRequestDispatcher("/WEB-INF/views/contract-detail.jsp").forward(request, response);
    }
}
