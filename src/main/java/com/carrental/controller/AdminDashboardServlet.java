package com.carrental.controller;

import com.carrental.dao.UserDAO;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final Set<String> USER_STATUSES = Set.of("ACTIVE", "LOCKED", "DISABLED");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers();
        request.setAttribute("users", users);
        request.setAttribute("allRoles", userDAO.getAllRoleNames());

        Map<Integer, List<String>> userRolesMap = new HashMap<>();
        for (User u : users) {
            userRolesMap.put(u.getUserId(), userDAO.getUserRoles(u.getUserId()));
        }
        request.setAttribute("userRolesMap", userRolesMap);

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
                    ok ? "Tao tai khoan thanh cong!" : "Tao tai khoan that bai!");
        } else if ("updateStatus".equals(action)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String status = request.getParameter("status");
            boolean ok = userDAO.updateUserStatus(userId, status);
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? "Cap nhat trang thai thanh cong!" : "Cap nhat that bai!");
        } else if ("updateUserDetails".equals(action)) {
            updateUserDetails(request, session, userDAO);
        } else if ("updateRoles".equals(action)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String[] roles = request.getParameterValues("roles");
            List<String> roleList = roles != null ? Arrays.asList(roles) : new ArrayList<>();
            boolean ok = userDAO.updateUserRoles(userId, roleList);
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                    ok ? "Cap nhat quyen thanh cong!" : "Cap nhat that bai!");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    private void updateUserDetails(HttpServletRequest request, HttpSession session, UserDAO userDAO) {
        int userId = Integer.parseInt(request.getParameter("userId"));
        String status = normalize(request.getParameter("status"));
        String fullName = normalize(request.getParameter("fullName"));
        String email = normalize(request.getParameter("email"));

        if (isBlank(fullName) || isBlank(email)) {
            session.setAttribute("flashError", "Ho ten va email khong duoc de trong.");
            return;
        }
        if (!USER_STATUSES.contains(status)) {
            session.setAttribute("flashError", "Trang thai tai khoan khong hop le.");
            return;
        }

        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(normalize(request.getParameter("phone")));
        user.setAddress(normalize(request.getParameter("address")));
        user.setIdentityNumber(normalize(request.getParameter("identityNumber")));
        user.setStatus(status);

        boolean ok = userDAO.updateUserByAdmin(user);
        session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Cap nhat thong tin tai khoan thanh cong!" : "Cap nhat thong tin tai khoan that bai!");
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
