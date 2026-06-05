package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/my-contracts")
public class CustomerContractsServlet extends HttpServlet {

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
        }

        // Flash messages
        String flash = (String) session.getAttribute("flashSuccess");
        if (flash != null) {
            request.setAttribute("success", flash);
            session.removeAttribute("flashSuccess");
        }

        request.getRequestDispatcher("/WEB-INF/views/my-contracts.jsp").forward(request, response);
    }
}
