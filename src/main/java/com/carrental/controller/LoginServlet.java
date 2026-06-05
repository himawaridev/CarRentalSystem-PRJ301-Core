package com.carrental.controller;

import com.carrental.dao.UserDAO;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If already logged in, redirect
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            response.sendRedirect(request.getContextPath() + "/search");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(username, password);

        if (user == null) {
            request.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("loggedInUser", user);

        List<String> roles = userDAO.getUserRoles(user.getUserId());
        session.setAttribute("userRoles", roles);

        // Check redirect parameter
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isEmpty() && redirect.startsWith("/")) {
            response.sendRedirect(redirect);
            return;
        }

        // Role-based routing
        if (roles.contains("ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else if (roles.contains("MANAGER")) {
            response.sendRedirect(request.getContextPath() + "/manager/dashboard");
        } else if (roles.contains("STAFF")) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
        } else if (roles.contains("DRIVER")) {
            response.sendRedirect(request.getContextPath() + "/driver/schedule");
        } else {
            response.sendRedirect(request.getContextPath() + "/search");
        }
    }
}
