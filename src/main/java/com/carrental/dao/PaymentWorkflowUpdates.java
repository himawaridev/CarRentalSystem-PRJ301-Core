package com.carrental.dao;

import com.carrental.model.ContractStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class PaymentWorkflowUpdates {
    private PaymentWorkflowUpdates() {
    }

    static void markTransactionPaid(Connection conn, long paymentTransactionId, String providerPaymentRef)
            throws SQLException {
        String sql = "UPDATE dbo.Payment_Transactions "
                + "SET Status = N'PAID', PaidAt = SYSUTCDATETIME(), ProviderPaymentRef = ?, UpdatedAt = SYSUTCDATETIME() "
                + "WHERE PaymentTransactionID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerPaymentRef);
            ps.setLong(2, paymentTransactionId);
            ps.executeUpdate();
        }
    }

    static void markPaymentLinesPaid(Connection conn, long paymentTransactionId, String providerPaymentRef)
            throws SQLException {
        String sql = "UPDATE dbo.Payments "
                + "SET PaymentStatus = N'PAID', PaidAt = SYSUTCDATETIME(), TransactionRef = ? "
                + "WHERE PaymentTransactionID = ? AND PaymentStatus = N'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerPaymentRef);
            ps.setLong(2, paymentTransactionId);
            ps.executeUpdate();
        }
    }

    static boolean hasPaidDeposit(Connection conn, long paymentTransactionId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM dbo.Payments "
                + "WHERE PaymentTransactionID = ? AND PaymentType = N'DEPOSIT' AND PaymentStatus = N'PAID'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentTransactionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    static void reserveContractAfterDeposit(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Contracts "
                + "SET Status = N'RESERVED', DepositPaidAt = COALESCE(DepositPaidAt, SYSUTCDATETIME()), "
                + "UpdatedAt = SYSUTCDATETIME() "
                + "WHERE ContractID = ? AND Status = N'PENDING_PAYMENT'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                insertStatusHistory(conn, contractId, ContractStatus.PENDING_PAYMENT, ContractStatus.RESERVED,
                        "Deposit payment confirmed.");
            }
        }
    }

    static void markFinalPaidIfPrepaid(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Contracts "
                + "SET FinalPaidAt = SYSUTCDATETIME(), UpdatedAt = SYSUTCDATETIME() "
                + "WHERE ContractID = ? "
                + "AND FinalAmountDue <= ("
                + "    SELECT COALESCE(SUM(Amount), 0) FROM dbo.Payments "
                + "    WHERE ContractID = ? "
                + "    AND PaymentType IN (N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID', N'RENTAL_BALANCE') "
                + "    AND PaymentStatus = N'PAID'"
                + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setLong(2, contractId);
            ps.executeUpdate();
        }
    }

    static void expireTransaction(Connection conn, long paymentTransactionId, long contractId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Payment_Transactions SET Status = N'EXPIRED', UpdatedAt = SYSUTCDATETIME() "
                        + "WHERE PaymentTransactionID = ?")) {
            ps.setLong(1, paymentTransactionId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Payments SET PaymentStatus = N'EXPIRED' "
                        + "WHERE PaymentTransactionID = ? AND PaymentStatus = N'PENDING'")) {
            ps.setLong(1, paymentTransactionId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Contracts SET Status = N'PAYMENT_EXPIRED', UpdatedAt = SYSUTCDATETIME() "
                        + "WHERE ContractID = ? AND Status = N'PENDING_PAYMENT'")) {
            ps.setLong(1, contractId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                insertStatusHistory(conn, contractId, ContractStatus.PENDING_PAYMENT,
                        ContractStatus.PAYMENT_EXPIRED, "Payment window expired.");
            }
        }
    }

    private static void insertStatusHistory(
            Connection conn,
            long contractId,
            String oldStatus,
            String newStatus,
            String note) throws SQLException {
        String sql = "INSERT INTO dbo.Contract_Status_History (ContractID, OldStatus, NewStatus, Note) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }
}
