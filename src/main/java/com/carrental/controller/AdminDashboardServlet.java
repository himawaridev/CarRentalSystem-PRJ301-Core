package com.carrental.controller;

import com.carrental.dao.UserDAO;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers();
        request.setAttribute("users", users);
        request.setAttribute("allRoles", userDAO.getAllRoleNames());

        // Load roles for each user
        Map<Integer, List<String>> userRolesMap = new HashMap<>();
        for (User u : users) {
            userRolesMap.put(u.getUserId(), userDAO.getUserRoles(u.getUserId()));
        }
        request.setAttribute("userRolesMap", userRolesMap);

        HttpSession session = request.getSession(false);
        if (session != null) {
            String flash = (String) session.getAttribute("flashSuccess");
            if (flash != null) { request.setAttribute("success", flash); session.removeAttribute("flashSuccess"); }
            String err = (String) session.getAttribute("flashError");
            if (err != null) { request.setAttribute("error", err); session.removeAttribute("flashError"); }
        }

        request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        UserDAO userDAO = new UserDAO();

        if ("createUser".equals(action)) {
            User user = new User();
            user.setUsername(request.getParameter("username"));
            user.setEmail(request.getParameter("email"));
            user.setPasswordHash(request.getParameter("password"));
            user.setFullName(request.getParameter("fullName"));
            user.setPhone(request.getParameter("phone"));
            user.setStatus("ACTIVE");

            String[] roles = request.getParameterValues("roles");
            List<String> roleList = roles != null ? Arrays.asList(roles) : List.of("CUSTOMER");

            boolean ok = userDAO.createUser(user, roleList);
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Tạo tài khoản thành công!" : "Tạo tài khoản thất bại!");
        } else if ("updateStatus".equals(action)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String status = request.getParameter("status");
            boolean ok = userDAO.updateUserStatus(userId, status);
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Cập nhật trạng thái thành công!" : "Cập nhật thất bại!");
        } else if ("updateRoles".equals(action)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String[] roles = request.getParameterValues("roles");
            List<String> roleList = roles != null ? Arrays.asList(roles) : new ArrayList<>();
            boolean ok = userDAO.updateUserRoles(userId, roleList);
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Cập nhật quyền thành công!" : "Cập nhật thất bại!");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}
