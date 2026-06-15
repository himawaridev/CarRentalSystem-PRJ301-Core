package com.carrental.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.carrental.model.User;
import java.io.IOException;
import java.util.List;

@WebFilter(urlPatterns = {"/book", "/profile", "/support", "/my-contracts", "/contract-detail", "/payment/*", "/staff/*", "/manager/*", "/driver/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;
        String path = request.getServletPath();

        if (path.startsWith("/payment/webhook")) {
            chain.doFilter(req, resp);
            return;
        }

        if (user == null) {
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String redirect = uri + (query != null ? "?" + query : "");
            response.sendRedirect(request.getContextPath() + "/login?error=auth&redirect="
                + java.net.URLEncoder.encode(redirect, "UTF-8"));
            return;
        }

        // Role-based access control
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute("userRoles");

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

    private boolean hasAnyRole(List<String> userRoles, String... required) {
        if (userRoles == null) return false;
        for (String r : required) {
            if (userRoles.contains(r)) return true;
        }
        return false;
    }
}
