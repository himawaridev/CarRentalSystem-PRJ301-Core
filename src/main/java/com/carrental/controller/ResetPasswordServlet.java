package com.carrental.controller;

import com.carrental.dao.AuthDAO;
import com.carrental.service.PasswordHasher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        attachFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = normalizeEmail(request.getParameter("email"));
        String code = normalizeCode(request.getParameter("code"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            forwardWithError(request, response, email, "Mat khau moi can co it nhat 8 ky tu.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            forwardWithError(request, response, email, "Mat khau xac nhan khong khop.");
            return;
        }

        AuthDAO.CodeResult result = new AuthDAO().resetPassword(email, code, PasswordHasher.hash(password));
        if (result == AuthDAO.CodeResult.SUCCESS) {
            HttpSession session = request.getSession();
            session.removeAttribute("devResetCode");
            session.setAttribute("flashSuccess", "Da dat lai mat khau. Hay dang nhap lai.");
            response.sendRedirect(request.getContextPath() + "/login?success=reset");
            return;
        }

        forwardWithError(request, response, email, messageFor(result));
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
        String devCode = (String) session.getAttribute("devResetCode");
        if (devCode != null) {
            request.setAttribute("devCode", devCode);
        }
    }

    private void forwardWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            String email,
            String error) throws ServletException, IOException {
        request.setAttribute("email", email);
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }

    private String messageFor(AuthDAO.CodeResult result) {
        return switch (result) {
            case NOT_FOUND -> "Ma dat lai mat khau khong ton tai.";
            case EXPIRED -> "Ma dat lai mat khau da het han. Hay yeu cau ma moi.";
            case TOO_MANY_ATTEMPTS -> "Ban da nhap sai qua nhieu lan. Hay yeu cau ma moi.";
            case INVALID -> "Ma dat lai mat khau khong dung.";
            default -> "Dat lai mat khau that bai. Vui long thu lai.";
        };
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }
}
