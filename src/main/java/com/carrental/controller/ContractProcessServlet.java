package com.carrental.controller;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.model.Contract;
import com.carrental.model.ContractStatus;
import com.carrental.model.Refund;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/staff/process")
public class ContractProcessServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        String contractIdStr = request.getParameter("contractId");
        String action = request.getParameter("action");

        if (contractIdStr == null || action == null) {
            session.setAttribute("flashError", "Thieu thong tin.");
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        long contractId = Long.parseLong(contractIdStr);
        ContractDAO dao = new ContractDAO();
        if ("cancel".equals(action)) {
            handleCancel(request, response, session, user, dao, contractId);
            return;
        }

        String newStatus;
        switch (action) {
            case "confirm", "accept" -> newStatus = ContractStatus.CONFIRMED;
            case "car_picked_up" -> newStatus = ContractStatus.CAR_PICKED_UP;
            case "car_returned" -> newStatus = ContractStatus.CAR_RETURNED;
            case "settlement_pending" -> newStatus = ContractStatus.SETTLEMENT_PENDING;
            case "complete", "final_payment" -> newStatus = ContractStatus.COMPLETED;
            default -> {
                session.setAttribute("flashError", "Hanh dong khong hop le.");
                response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                return;
            }
        }

        boolean ok = dao.updateContractStatus(contractId, newStatus, user.getUserId());
        session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Cap nhat hop dong #" + contractId + " thanh cong."
                   : "Cap nhat that bai. Vui long kiem tra trang thai hien tai.");

        response.sendRedirect(request.getContextPath() + "/staff/dashboard");
    }

    private void handleCancel(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            User user,
            ContractDAO contractDAO,
            long contractId) throws IOException {
        Contract contract = contractDAO.getContractById(contractId);
        if (contract == null) {
            session.setAttribute("flashError", "Khong tim thay hop dong.");
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
            return;
        }

        PaymentDAO paymentDAO = new PaymentDAO();
        Refund refundBeforeCancel = paymentDAO.getPendingRefundByContractId(contractId);
        boolean ok = paymentDAO.cancelContractWithRefund(
                contractId,
                user.getUserId(),
                "Nhan vien huy hop dong. Tao yeu cau hoan tien neu khach da thanh toan.");
        Refund refundAfterCancel = paymentDAO.getPendingRefundByContractId(contractId);

        if (!ok) {
            session.setAttribute("flashError",
                    "Khong the huy hop dong " + contract.getContractCode()
                    + ". Vui long kiem tra trang thai hien tai.");
        } else if (refundAfterCancel != null) {
            String refundMessage = refundBeforeCancel == null
                    ? " Yeu cau hoan tien da duoc tao. Bam icon hoan tien de xu ly QR/chuyen khoan cho khach."
                    : " Hop dong da co yeu cau hoan tien dang cho xu ly.";
            session.setAttribute("flashSuccess",
                    "Da huy hop dong " + contract.getContractCode() + "." + refundMessage);
        } else {
            session.setAttribute("flashSuccess",
                    "Da huy hop dong " + contract.getContractCode() + " thanh cong.");
        }

        response.sendRedirect(request.getContextPath() + "/staff/dashboard");
    }
}
