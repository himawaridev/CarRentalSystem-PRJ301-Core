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
    private static final Set<String> USER_STATUSES = Set.of("ACTIVE", "LOCKED", "DISABLED");
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
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers();
        request.setAttribute("users", users);
        request.setAttribute("allRoles", userDAO.getAllRoleNames());
        request.setAttribute("bankOptions", BANKS);

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
        } else if ("updateUserDetails".equals(action)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String status = normalize(request.getParameter("status"));
            String fullName = normalize(request.getParameter("fullName"));
            String email = normalize(request.getParameter("email"));
            String bankCode = normalize(request.getParameter("bankCode"));
            String bankAccountNumber = normalizeAccount(request.getParameter("bankAccountNumber"));
            String bankAccountHolder = normalize(request.getParameter("bankAccountHolder"));
            String bankName = isBlank(bankCode) ? null : BANKS.get(bankCode);

            if (isBlank(fullName) || isBlank(email)) {
                session.setAttribute("flashError", "Ho ten va email khong duoc de trong.");
            } else if (!USER_STATUSES.contains(status)) {
                session.setAttribute("flashError", "Trang thai tai khoan khong hop le.");
            } else if (!isBlank(bankCode) && bankName == null) {
                session.setAttribute("flashError", "Ngan hang khong hop le.");
            } else if (!isBlank(bankCode) || !isBlank(bankAccountNumber) || !isBlank(bankAccountHolder)) {
                if (isBlank(bankCode) || isBlank(bankAccountNumber) || isBlank(bankAccountHolder)) {
                    session.setAttribute("flashError",
                            "Neu sua tai khoan ngan hang, can nhap du ngan hang, so tai khoan va chu tai khoan.");
                } else if (!bankAccountNumber.matches("\\d{6,20}")) {
                    session.setAttribute("flashError", "So tai khoan chi duoc gom 6-20 chu so.");
                } else {
                    boolean ok = updateUserDetails(request, userDAO, userId, status, email, fullName,
                            bankCode, bankName, bankAccountNumber, bankAccountHolder);
                    session.setAttribute(ok ? "flashSuccess" : "flashError",
                            ok ? "Cap nhat thong tin tai khoan thanh cong!" : "Cap nhat thong tin tai khoan that bai!");
                }
            } else {
                boolean ok = updateUserDetails(request, userDAO, userId, status, email, fullName,
                        null, null, null, null);
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                        ok ? "Cap nhat thong tin tai khoan thanh cong!" : "Cap nhat thong tin tai khoan that bai!");
            }
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

    private boolean updateUserDetails(
            HttpServletRequest request,
            UserDAO userDAO,
            int userId,
            String status,
            String email,
            String fullName,
            String bankCode,
            String bankName,
            String bankAccountNumber,
            String bankAccountHolder) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(normalize(request.getParameter("phone")));
        user.setAddress(normalize(request.getParameter("address")));
        user.setIdentityNumber(normalize(request.getParameter("identityNumber")));
        user.setStatus(status);
        user.setBankCode(bankCode);
        user.setBankName(bankName);
        user.setBankAccountNumber(bankAccountNumber);
        user.setBankAccountHolder(bankAccountHolder);
        return userDAO.updateUserByAdmin(user);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeAccount(String value) {
        return value == null ? null : value.replaceAll("\\s+", "").trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
