package com.carrental.controller;

import com.carrental.dao.SupportTicketDAO;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/staff/support")
public class StaffSupportServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = normalize(request.getParameter("status"));
        SupportTicketDAO ticketDAO = new SupportTicketDAO();
        request.setAttribute("statusFilter", ticketDAO.isValidStatus(status) ? status : null);
        request.setAttribute("tickets", ticketDAO.getAllTickets(status));
        flash(request);
        request.getRequestDispatcher("/WEB-INF/views/staff-support.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        long ticketId = parseLong(request.getParameter("ticketId"));
        String status = normalize(request.getParameter("status"));
        String priority = normalize(request.getParameter("priority"));
        String staffResponse = normalize(request.getParameter("staffResponse"));

        SupportTicketDAO ticketDAO = new SupportTicketDAO();
        HttpSession session = request.getSession();
        if (ticketId <= 0 || !ticketDAO.isValidStatus(status) || !ticketDAO.isValidPriority(priority)) {
            session.setAttribute("flashError", "Thong tin cap nhat ticket khong hop le.");
        } else {
            boolean ok = ticketDAO.updateTicketByStaff(ticketId, status, priority, staffResponse, user.getUserId());
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? "Da cap nhat yeu cau ho tro." : "Khong the cap nhat yeu cau ho tro.");
        }

        response.sendRedirect(request.getContextPath() + "/staff/support");
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("loggedInUser");
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
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
