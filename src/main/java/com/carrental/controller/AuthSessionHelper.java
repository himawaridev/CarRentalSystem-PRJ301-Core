package com.carrental.controller;

import com.carrental.dao.UserDAO;
import com.carrental.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

final class AuthSessionHelper {
    private AuthSessionHelper() {
    }

    static void signIn(
            HttpServletRequest request,
            HttpServletResponse response,
            User user,
            String redirect) throws IOException {
        UserDAO userDAO = new UserDAO();
        HttpSession session = request.getSession();
        session.setAttribute("loggedInUser", user);

        List<String> roles = userDAO.getUserRoles(user.getUserId());
        session.setAttribute("userRoles", roles);

        if (redirect != null && !redirect.isEmpty() && redirect.startsWith("/")) {
            response.sendRedirect(redirect);
            return;
        }

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
