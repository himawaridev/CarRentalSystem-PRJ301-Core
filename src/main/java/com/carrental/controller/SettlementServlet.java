package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.SettlementResult;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/staff/settlement")
public class SettlementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long contractId = parseContractId(request);
        if (contractId <= 0) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        ContractDAO contractDAO = new ContractDAO();
        Contract contract = contractDAO.getContractById(contractId);
        if (contract == null) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        request.setAttribute("contract", contract);
        request.setAttribute("settlement", paymentDAO.calculateSettlement(contractId));
        request.setAttribute("paymentRecords", paymentDAO.getPaymentRecordsByContractId(contractId));

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

        request.getRequestDispatcher("/WEB-INF/views/settlement.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");
        long contractId = parseContractId(request);
        String action = request.getParameter("action");

        if (contractId <= 0 || action == null) {
            session.setAttribute("flashError", "Thieu thong tin quyet toan.");
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        if ("collectBalance".equals(action)) {
            SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
            BigDecimal amountToCollect = settlement.getAmountToCollect();
            if (amountToCollect.compareTo(BigDecimal.ZERO) <= 0) {
                session.setAttribute("flashError", "Khong con so tien can thu them.");
            } else {
                boolean ok = paymentDAO.recordRentalBalancePayment(
                        contractId,
                        amountToCollect,
                        user.getUserId(),
                        "Staff recorded remaining rental/driver fee at settlement.");
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                        ok ? "Da ghi nhan thu tien con lai." : "Khong the ghi nhan thanh toan.");
            }
        } else if ("complete".equals(action)) {
            SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
            if (settlement.getAmountToCollect().compareTo(BigDecimal.ZERO) == 0) {
                ContractDAO contractDAO = new ContractDAO();
                boolean ok = contractDAO.updateContractStatus(contractId, ContractStatus.COMPLETED, user.getUserId());
                session.setAttribute(ok ? "flashSuccess" : "flashError",
                        ok ? "Hop dong da hoan tat." : "Khong the hoan tat hop dong.");
                if (ok) {
                    response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                    return;
                }
            } else {
                session.setAttribute("flashError", "Van con tien thue/tai xe can thu.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/staff/settlement?contractId=" + contractId);
    }

    private long parseContractId(HttpServletRequest request) {
        return parseLong(request.getParameter("contractId"));
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }
}
