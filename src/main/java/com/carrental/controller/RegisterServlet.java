package com.carrental.controller;

import com.carrental.dao.AuthDAO;
import com.carrental.dao.UserDAO;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int CODE_TTL_MINUTES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        attachFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = normalize(request.getParameter("username"));
        String email = normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");
        String fullName = normalize(request.getParameter("fullName"));
        String phone = normalize(request.getParameter("phone"));
        String address = normalize(request.getParameter("address"));

        UserDAO userDAO = new UserDAO();
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(fullName)) {
            forwardWithError(request, response, "Vui long nhap day du thong tin bat buoc.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            forwardWithError(request, response, "Mat khau can co it nhat 8 ky tu.");
            return;
        }
        if (userDAO.usernameExists(username)) {
            forwardWithError(request, response, "Ten dang nhap da ton tai.");
            return;
        }
        if (userDAO.emailExists(email)) {
            forwardWithError(request, response, "Email da duoc su dung.");
            return;
        }

        String code = PasswordHasher.randomNumericCode();
        AuthDAO authDAO = new AuthDAO();
        boolean saved = authDAO.savePendingRegistration(
                username,
                email,
                PasswordHasher.hash(password),
                fullName,
                phone,
                address,
                PasswordHasher.hash(code),
                LocalDateTime.now(ZoneOffset.UTC).plusMinutes(CODE_TTL_MINUTES));
        if (!saved) {
            forwardWithError(request, response, "Khong the tao yeu cau xac minh. Vui long thu lai.");
            return;
        }

        AuthConfig config = new AuthConfig();
        boolean sent = new EmailService(config).sendVerificationCode(email, fullName, code);
        if (!sent && !config.devMode()) {
            forwardWithError(request, response,
                    "Chua cau hinh SMTP nen khong gui duoc ma xac minh email.");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("flashSuccess",
                "Da gui ma xac minh toi email. Vui long nhap ma de hoan tat dang ky.");
        if (config.devMode()) {
            session.setAttribute("devVerificationCode", code);
        }
        response.sendRedirect(request.getContextPath() + "/verify-email?email="
                + URLEncoder.encode(email, StandardCharsets.UTF_8));
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private void attachFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String error = (String) session.getAttribute("flashError");
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute("flashError");
        }
        String success = (String) session.getAttribute("flashSuccess");
        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("flashSuccess");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(java.util.Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
