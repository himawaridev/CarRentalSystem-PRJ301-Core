package com.carrental.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.carrental.model.User;
import com.carrental.dao.UserDAO;
import java.io.IOException;
import java.util.List;

@WebFilter(urlPatterns = {"/book", "/my-contracts", "/contract-detail", "/staff/*", "/manager/*", "/driver/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;
        String path = request.getServletPath();

        if (user == null) {
            redirectToLogin(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User currentUser = userDAO.getUserById(user.getUserId());
        if (currentUser == null || !"ACTIVE".equals(currentUser.getStatus())
                || !currentUser.getUsername().equals(user.getUsername())) {
            session.invalidate();
            redirectToLogin(request, response);
            return;
        }

        List<String> roles = userDAO.getUserRoles(currentUser.getUserId());
        session.setAttribute("loggedInUser", currentUser);
        session.setAttribute("userRoles", roles);

        if ((path.equals("/book") || path.equals("/my-contracts"))
                && !hasAnyRole(roles, "CUSTOMER")) {
            session.setAttribute("flashError",
                    "Chi tai khoan khach hang moi co the dat xe. Vui long dang nhap bang tai khoan khach hang.");
            response.sendRedirect(request.getContextPath() + "/search");
            return;
        }

        if (path.startsWith("/staff") && !hasAnyRole(roles, "STAFF", "MANAGER", "ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        if (path.startsWith("/manager") && !hasAnyRole(roles, "MANAGER", "ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        if (path.startsWith("/driver") && !hasAnyRole(roles, "DRIVER", "ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        if (path.startsWith("/admin") && !hasAnyRole(roles, "ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        chain.doFilter(req, resp);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String redirect = uri + (query != null ? "?" + query : "");
        response.sendRedirect(request.getContextPath() + "/login?error=auth&redirect="
                + java.net.URLEncoder.encode(redirect, "UTF-8"));
    }

    private boolean hasAnyRole(List<String> userRoles, String... required) {
        if (userRoles == null) return false;
        for (String r : required) {
            if (userRoles.contains(r)) return true;
        }
        return false;
    }
}
