package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.SupportTicketDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Contract;
import com.carrental.model.SupportTicket;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/support")
public class SupportServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        UserDAO userDAO = new UserDAO();
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());
        if (customerId != null) {
            request.setAttribute("contracts", new ContractDAO().getContractsByCustomerId(customerId));
        }
        request.setAttribute("tickets", new SupportTicketDAO().getTicketsByUserId(user.getUserId()));
        request.setAttribute("selectedContractId", parseLongOrNull(request.getParameter("contractId")));
        flash(request);
        request.getRequestDispatcher("/WEB-INF/views/support.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        String category = normalize(request.getParameter("category"));
        String subject = normalize(request.getParameter("subject"));
        String message = normalize(request.getParameter("message"));
        Long contractId = parseLongOrNull(request.getParameter("contractId"));

        HttpSession session = request.getSession();
        SupportTicketDAO ticketDAO = new SupportTicketDAO();
        if (!ticketDAO.isValidCategory(category)) {
            session.setAttribute("flashError", "Loai yeu cau ho tro khong hop le.");
        } else if (isBlank(subject) || isBlank(message)) {
            session.setAttribute("flashError", "Vui long nhap tieu de va noi dung can ho tro.");
        } else if (contractId != null && !ownsContract(user.getUserId(), contractId)) {
            session.setAttribute("flashError", "Hop dong duoc chon khong thuoc tai khoan cua ban.");
        } else {
            SupportTicket ticket = new SupportTicket();
            ticket.setUserId(user.getUserId());
            ticket.setContractId(contractId);
            ticket.setCategory(category);
            ticket.setSubject(subject);
            ticket.setMessage(message);
            boolean ok = ticketDAO.createTicket(ticket);
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? "Da gui yeu cau ho tro. Nhan vien se xu ly trong thoi gian som nhat."
                            : "Khong the tao yeu cau ho tro. Vui long thu lai.");
        }

        response.sendRedirect(request.getContextPath() + "/support");
    }

    private boolean ownsContract(int userId, long contractId) {
        Integer customerId = new UserDAO().getCustomerIdByUserId(userId);
        if (customerId == null) {
            return false;
        }
        Contract contract = new ContractDAO().getContractById(contractId);
        return contract != null && contract.getCustomerId() == customerId;
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("loggedInUser");
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void flash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String success = (String) session.getAttribute("flashSuccess");
        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("flashSuccess");
        }
        String error = (String) session.getAttribute("flashError");
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute("flashError");
        }
    }
}
