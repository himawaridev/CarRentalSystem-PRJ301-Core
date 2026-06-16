package com.carrental.controller;

import com.carrental.dao.AuthDAO;
import com.carrental.model.User;
import com.carrental.service.AuthConfig;
import com.carrental.service.EmailService;
import com.carrental.service.PasswordHasher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private static final int CODE_TTL_MINUTES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = normalizeEmail(request.getParameter("email"));
        AuthDAO authDAO = new AuthDAO();
        User user = authDAO.findActiveUserByEmail(email);

        if (user != null) {
            String code = PasswordHasher.randomNumericCode();
            boolean saved = authDAO.savePasswordResetCode(
                    email,
                    PasswordHasher.hash(code),
                    LocalDateTime.now(ZoneOffset.UTC).plusMinutes(CODE_TTL_MINUTES));
            AuthConfig config = new AuthConfig();
            boolean sent = saved && new EmailService(config).sendPasswordResetCode(
                    email, user.getFullName(), code);
            if (config.devMode()) {
                request.getSession().setAttribute("devResetCode", code);
            } else if (!sent) {
                request.setAttribute("error", "Chua cau hinh SMTP nen khong gui duoc ma dat lai mat khau.");
                request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
                return;
            }
        }

        HttpSession session = request.getSession();
        session.setAttribute("flashSuccess",
                "Neu email ton tai, chung toi da gui ma dat lai mat khau.");
        response.sendRedirect(request.getContextPath() + "/reset-password?email="
                + URLEncoder.encode(email == null ? "" : email, StandardCharsets.UTF_8));
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
