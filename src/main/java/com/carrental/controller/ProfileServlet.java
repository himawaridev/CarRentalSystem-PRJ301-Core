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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final Map<String, String> BANKS = new LinkedHashMap<>();

    static {
        BANKS.put("MB", "MB Bank");
        BANKS.put("VCB", "Vietcombank");
        BANKS.put("TCB", "Techcombank");
        BANKS.put("ACB", "ACB");
        BANKS.put("BIDV", "BIDV");
        BANKS.put("CTG", "VietinBank");
        BANKS.put("VPB", "VPBank");
        BANKS.put("TPB", "TPBank");
        BANKS.put("VIB", "VIB");
        BANKS.put("MBB", "MB Bank");
        BANKS.put("OCB", "OCB");
        BANKS.put("HDB", "HDBank");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User sessionUser = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        User user = new UserDAO().getUserById(sessionUser.getUserId());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        request.setAttribute("profileUser", user);
        request.setAttribute("bankOptions", BANKS);
        request.setAttribute("redirect", safeRedirect(request.getParameter("redirect"), request.getContextPath()));
        request.setAttribute("requiredBank", "bank".equalsIgnoreCase(request.getParameter("required")));
        readFlash(session, request);

        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User current = userDAO.getUserById(sessionUser.getUserId());
        if (current == null) {
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        String fullName = normalize(request.getParameter("fullName"));
        String phone = normalize(request.getParameter("phone"));
        String address = normalize(request.getParameter("address"));
        String bankCode = normalize(request.getParameter("bankCode"));
        String bankName = BANKS.get(bankCode);
        String bankAccountNumber = normalizeAccount(request.getParameter("bankAccountNumber"));
        String bankAccountHolder = normalizeAccountHolder(request.getParameter("bankAccountHolder"));

        if (isBlank(fullName)) {
            forwardWithError(request, response, current, "Vui long nhap ho ten.");
            return;
        }

        if (!current.isBankInfoLocked()) {
            if (isBlank(bankCode) || isBlank(bankName) || isBlank(bankAccountNumber)
                    || isBlank(bankAccountHolder)) {
                forwardWithError(request, response, current,
                        "Vui long cap nhat day du ngan hang, so tai khoan va ten chu tai khoan.");
                return;
            }
            if (!bankAccountNumber.matches("\\d{6,20}")) {
                forwardWithError(request, response, current,
                        "So tai khoan chi duoc gom 6-20 chu so. Hay kiem tra that ky truoc khi luu.");
                return;
            }
        }

        boolean ok = userDAO.updateCustomerProfile(
                current.getUserId(),
                fullName,
                phone,
                address,
                bankCode,
                bankName,
                bankAccountNumber,
                bankAccountHolder);

        if (!ok) {
            forwardWithError(request, response, current, "Cap nhat ho so that bai. Vui long thu lai.");
            return;
        }

        User updated = userDAO.getUserById(current.getUserId());
        session.setAttribute("loggedInUser", updated);
        session.setAttribute("flashSuccess", "Da cap nhat ho so thanh cong.");

        String redirect = safeRedirect(request.getParameter("redirect"), request.getContextPath());
        response.sendRedirect(redirect == null ? request.getContextPath() + "/profile" : redirect);
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, User user, String error)
            throws ServletException, IOException {
        request.setAttribute("profileUser", user);
        request.setAttribute("bankOptions", BANKS);
        request.setAttribute("error", error);
        request.setAttribute("redirect", safeRedirect(request.getParameter("redirect"), request.getContextPath()));
        request.setAttribute("requiredBank", "bank".equalsIgnoreCase(request.getParameter("required")));
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    private void readFlash(HttpSession session, HttpServletRequest request) {
        String success = (String) session.getAttribute("flashSuccess");
        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("flashSuccess");
        }
        String error = (String) session.getAttribute("flashError");
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute("flashError");
        }
    }

    private String safeRedirect(String redirect, String contextPath) {
        if (redirect == null || redirect.isBlank()) {
            return null;
        }
        return redirect.startsWith(contextPath + "/") ? redirect : null;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeAccount(String value) {
        return value == null ? null : value.replaceAll("\\s+", "").trim();
    }

    private String normalizeAccountHolder(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
