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

    static void markDepositRefundPending(Connection conn, long paymentId) throws SQLException {
        String sql = "UPDATE dbo.Payments SET PaymentStatus = N'REFUND_PENDING' WHERE PaymentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            ps.executeUpdate();
        }
    }

    static void moveContractToSettlementPending(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Contracts SET Status = N'SETTLEMENT_PENDING', UpdatedAt = SYSUTCDATETIME() "
                + "WHERE ContractID = ? AND Status = N'CAR_RETURNED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                insertStatusHistory(conn, contractId, ContractStatus.CAR_RETURNED,
                        ContractStatus.SETTLEMENT_PENDING, "Refund request created.");
            }
        }
    }

    static void tryCompleteSettledContract(Connection conn, long contractId) throws SQLException {
        String oldStatus = getContractStatus(conn, contractId);
        if (!ContractStatus.CAR_RETURNED.equals(oldStatus)
                && !ContractStatus.SETTLEMENT_PENDING.equals(oldStatus)) {
            return;
        }

        String sql = "UPDATE dbo.Contracts SET Status = N'COMPLETED', UpdatedAt = SYSUTCDATETIME() "
                + "WHERE ContractID = ? AND Status = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setString(2, oldStatus);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                insertStatusHistory(conn, contractId, oldStatus, ContractStatus.COMPLETED, "Refund completed.");
            }
        }
    }

    static String getContractStatus(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT Status FROM dbo.Contracts WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Status") : null;
            }
        }
    }

    static String getContractStatusForUpdate(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT Status FROM dbo.Contracts WITH (UPDLOCK, ROWLOCK) WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Status") : null;
            }
        }
    }

    static void markCancellationPaymentsRefundPending(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Payments SET PaymentStatus = N'REFUND_PENDING' "
                + "WHERE ContractID = ? "
                + "AND PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                + "AND PaymentStatus = N'PAID'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    static void updateCancelledContractPaymentsAfterRefund(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Payments SET PaymentStatus = N'REFUNDED' "
                + "WHERE ContractID = ? "
                + "AND PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                + "AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    static void cancelContract(
            Connection conn,
            long contractId,
            String oldStatus,
            int changedByUserId,
            String note) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Contracts SET Status = N'CANCELLED', ReviewedByUserID = ?, "
                        + "ReviewedAt = SYSUTCDATETIME(), UpdatedAt = SYSUTCDATETIME() "
                        + "WHERE ContractID = ? AND Status = ?")) {
            ps.setInt(1, changedByUserId);
            ps.setLong(2, contractId);
            ps.setString(3, oldStatus);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Contract_Details SET DetailStatus = N'CANCELLED' "
                        + "WHERE ContractID = ? AND DetailStatus <> N'CANCELLED'")) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Payment_Transactions SET Status = N'EXPIRED', UpdatedAt = SYSUTCDATETIME() "
                        + "WHERE ContractID = ? AND Status = N'PENDING'")) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Payments SET PaymentStatus = N'EXPIRED' "
                        + "WHERE ContractID = ? AND PaymentStatus = N'PENDING'")) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
        insertStatusHistory(conn, contractId, oldStatus, ContractStatus.CANCELLED, note);
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
