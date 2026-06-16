package com.carrental.controller;

import com.carrental.dao.AuthDAO;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@WebServlet("/verify-email")
public class VerifyEmailServlet extends HttpServlet {
    private static final int CODE_TTL_MINUTES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        attachFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/verify-email.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = normalizeEmail(request.getParameter("email"));
        String action = request.getParameter("action");
        if ("resend".equals(action)) {
            resendCode(request, response, email);
            return;
        }

        String code = normalizeCode(request.getParameter("code"));
        AuthDAO.CodeResult result = new AuthDAO().completePendingRegistration(email, code);
        if (result == AuthDAO.CodeResult.SUCCESS) {
            HttpSession session = request.getSession();
            session.removeAttribute("devVerificationCode");
            session.setAttribute("flashSuccess",
                    "Xac minh email thanh cong. Hay dang nhap bang tai khoan vua tao.");
            response.sendRedirect(request.getContextPath() + "/login?success=verified");
            return;
        }

        request.setAttribute("email", email);
        request.setAttribute("error", messageFor(result));
        request.getRequestDispatcher("/WEB-INF/views/verify-email.jsp").forward(request, response);
    }

    private void resendCode(HttpServletRequest request, HttpServletResponse response, String email)
            throws ServletException, IOException {
        AuthDAO authDAO = new AuthDAO();
        AuthDAO.PendingRegistration pending = authDAO.findPendingRegistration(email);
        if (pending == null) {
            request.setAttribute("email", email);
            request.setAttribute("error", "Khong tim thay yeu cau dang ky dang cho xac minh.");
            request.getRequestDispatcher("/WEB-INF/views/verify-email.jsp").forward(request, response);
            return;
        }

        String code = PasswordHasher.randomNumericCode();
        boolean saved = authDAO.savePendingRegistration(
                pending.getUsername(),
                pending.getEmail(),
                pending.getPasswordHash(),
                pending.getFullName(),
                pending.getPhone(),
                pending.getAddress(),
                PasswordHasher.hash(code),
                LocalDateTime.now(ZoneOffset.UTC).plusMinutes(CODE_TTL_MINUTES));
        AuthConfig config = new AuthConfig();
        boolean sent = saved && new EmailService(config).sendVerificationCode(
                pending.getEmail(), pending.getFullName(), code);
        if (!sent && !config.devMode()) {
            request.setAttribute("error", "Chua cau hinh SMTP nen khong gui duoc ma xac minh.");
        } else {
            request.setAttribute("success", "Da gui lai ma xac minh.");
            if (config.devMode()) {
                request.getSession().setAttribute("devVerificationCode", code);
            }
        }
        request.setAttribute("email", email);
        request.getRequestDispatcher("/WEB-INF/views/verify-email.jsp").forward(request, response);
    }

    private void attachFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String flash = (String) session.getAttribute("flashSuccess");
        if (flash != null) {
            request.setAttribute("success", flash);
            session.removeAttribute("flashSuccess");
        }
        String devCode = (String) session.getAttribute("devVerificationCode");
        if (devCode != null) {
            request.setAttribute("devCode", devCode);
        }
    }

    private String messageFor(AuthDAO.CodeResult result) {
        return switch (result) {
            case NOT_FOUND -> "Khong tim thay yeu cau dang ky dang cho xac minh.";
            case EXPIRED -> "Ma xac minh da het han. Hay gui lai ma moi.";
            case TOO_MANY_ATTEMPTS -> "Ban da nhap sai qua nhieu lan. Hay gui lai ma moi.";
            case INVALID -> "Ma xac minh khong dung.";
            case DUPLICATE -> "Ten dang nhap hoac email da duoc su dung.";
            default -> "Xac minh that bai. Vui long thu lai.";
        };
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }
}
