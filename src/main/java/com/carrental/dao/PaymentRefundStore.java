package com.carrental.dao;

import com.carrental.model.PaymentStatus;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class PaymentRefundStore {
    private static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    private static final String METHOD_CASH = "CASH";
    private static final String METHOD_OTHER = "OTHER";

    private PaymentRefundStore() {
    }

    static BigDecimal sumRefundedAmount(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(RefundAmount), 0) FROM dbo.Refunds "
                + "WHERE ContractID = ? AND Status = N'REFUNDED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? PaymentAmounts.safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }

    static Refund insert(
            Connection conn,
            SettlementResult settlement,
            int approvedByUserId,
            String reason,
            RefundMethod refundMethod)
            throws SQLException {
        String sql = "INSERT INTO dbo.Refunds "
                + "(ContractID, SourcePaymentID, DepositAmount, DeductionAmount, RefundAmount, Reason, "
                + "RefundMethod, Status, ApprovedByUserID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, N'REFUND_PENDING', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, settlement.getContractId());
            ps.setLong(2, settlement.getSourcePaymentId());
            ps.setBigDecimal(3, settlement.getDepositPaid());
            ps.setBigDecimal(4, settlement.getDeductionAmount());
            ps.setBigDecimal(5, settlement.getRefundAmount());
            ps.setString(6, reason);
            ps.setString(7, refundMethod.name());
            ps.setInt(8, approvedByUserId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Cannot create refund.");
                }
                Refund refund = new Refund();
                refund.setRefundId(keys.getLong(1));
                refund.setContractId(settlement.getContractId());
                refund.setSourcePaymentId(settlement.getSourcePaymentId());
                refund.setDepositAmount(settlement.getDepositPaid());
                refund.setDeductionAmount(settlement.getDeductionAmount());
                refund.setRefundAmount(settlement.getRefundAmount());
                refund.setReason(reason);
                refund.setRefundMethod(refundMethod);
                refund.setStatus(PaymentStatus.REFUND_PENDING);
                refund.setApprovedByUserId(approvedByUserId);
                return refund;
            }
        }
    }

    static Refund lock(Connection conn, long refundId) throws SQLException {
        String sql = "SELECT * FROM dbo.Refunds WITH (UPDLOCK, ROWLOCK) WHERE RefundID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, refundId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return PaymentRowMapper.mapRefund(rs);
            }
        }
    }

    static void markCompleted(
            Connection conn,
            long refundId,
            RefundMethod refundMethod,
            String providerRefundRef,
            String proofOfRefund,
            Integer completedByUserId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE dbo.Refunds SET Status = N'REFUNDED', RefundMethod = ?, "
                        + "ProofOfRefund = ?, ProviderRefundRef = ?, CompletedByUserID = ?, "
                        + "CompletedAt = SYSUTCDATETIME(), UpdatedAt = SYSUTCDATETIME() "
                        + "WHERE RefundID = ?")) {
            ps.setString(1, refundMethod.name());
            ps.setString(2, proofOfRefund);
            ps.setString(3, providerRefundRef);
            if (completedByUserId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, completedByUserId);
            }
            ps.setLong(5, refundId);
            ps.executeUpdate();
        }
    }

    static void insertRefundPaymentLine(Connection conn, Refund refund, String providerRefundRef) throws SQLException {
        if (PaymentAmounts.safe(refund.getRefundAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String sql = "INSERT INTO dbo.Payments "
                + "(ContractID, SourcePaymentID, PaymentType, Amount, PaymentMethod, PaymentStatus, "
                + "PaidAt, TransactionRef, Note) "
                + "VALUES (?, ?, N'REFUND', ?, ?, N'REFUNDED', SYSUTCDATETIME(), ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, refund.getContractId());
            ps.setLong(2, refund.getSourcePaymentId());
            ps.setBigDecimal(3, refund.getRefundAmount());
            ps.setString(4, paymentMethodForRefund(refund.getRefundMethod()));
            ps.setString(5, providerRefundRef);
            ps.setString(6, "Deposit refund completed by " + refund.getRefundMethod());
            ps.executeUpdate();
        }
    }

    static void updateSourcePaymentAfterRefund(Connection conn, Refund refund) throws SQLException {
        BigDecimal settledDeposit = PaymentAmounts.safe(refund.getRefundAmount())
                .add(PaymentAmounts.safe(refund.getDeductionAmount()));
        String newStatus = settledDeposit.compareTo(PaymentAmounts.safe(refund.getDepositAmount())) >= 0
                ? PaymentStatus.REFUNDED.name()
                : PaymentStatus.PARTIALLY_REFUNDED.name();

        String sql = "UPDATE dbo.Payments SET PaymentStatus = ? WHERE PaymentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, refund.getSourcePaymentId());
            ps.executeUpdate();
        }
    }

    static boolean markGatewayFailed(Connection conn, long refundId, String errorMessage) throws SQLException {
        String sql = "UPDATE dbo.Refunds SET RefundMethod = N'GATEWAY_REFUND', ProofOfRefund = ?, "
                + "UpdatedAt = SYSUTCDATETIME() "
                + "WHERE RefundID = ? AND Status = N'REFUND_PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, errorMessage);
            ps.setLong(2, refundId);
            return ps.executeUpdate() > 0;
        }
    }

    static Refund findPending(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT TOP 1 * FROM dbo.Refunds "
                + "WHERE ContractID = ? AND Status = N'REFUND_PENDING' "
                + "ORDER BY RefundID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapRefund(rs);
                }
            }
        }
        return null;
    }

    static Refund findById(Connection conn, long refundId) throws SQLException {
        String sql = "SELECT * FROM dbo.Refunds WHERE RefundID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, refundId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapRefund(rs);
                }
            }
        }
        return null;
    }

    static Refund findLatestByContractId(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT TOP 1 * FROM dbo.Refunds "
                + "WHERE ContractID = ? "
                + "ORDER BY CreatedAt DESC, RefundID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapRefund(rs);
                }
            }
        }
        return null;
    }

    static BigDecimal calculateCancellationRefundAmount(Connection conn, long contractId) throws SQLException {
        BigDecimal refundablePaid = sumPayments(conn, contractId,
                "PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                        + "AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED')");
        BigDecimal alreadyRefunding = sumRefundAmount(conn, contractId,
                "Status IN (N'REFUND_PENDING', N'REFUNDED')");
        return PaymentAmounts.maxZero(refundablePaid.subtract(alreadyRefunding));
    }

    static Long findLatestRefundablePaymentId(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT TOP 1 PaymentID FROM dbo.Payments "
                + "WHERE ContractID = ? "
                + "AND PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                + "AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED') "
                + "ORDER BY CASE WHEN PaymentType = N'DEPOSIT' THEN 0 ELSE 1 END, PaidAt DESC, PaymentID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("PaymentID") : null;
            }
        }
    }

    static Long findLatestDepositPaymentId(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT TOP 1 PaymentID FROM dbo.Payments "
                + "WHERE ContractID = ? AND PaymentType = N'DEPOSIT' "
                + "AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED') "
                + "ORDER BY PaidAt DESC, PaymentID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("PaymentID");
                }
            }
        }
        return null;
    }

    private static String paymentMethodForRefund(RefundMethod refundMethod) {
        if (refundMethod == RefundMethod.CASH_AT_COUNTER) {
            return METHOD_CASH;
        }
        if (refundMethod == RefundMethod.WALLET_CREDIT) {
            return METHOD_OTHER;
        }
        return METHOD_BANK_TRANSFER;
    }

    private static BigDecimal sumPayments(Connection conn, long contractId, String filter) throws SQLException {
        String sql = "SELECT COALESCE(SUM(Amount), 0) FROM dbo.Payments WHERE ContractID = ? AND " + filter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? PaymentAmounts.safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }

    private static BigDecimal sumRefundAmount(Connection conn, long contractId, String filter) throws SQLException {
        String sql = "SELECT COALESCE(SUM(RefundAmount), 0) FROM dbo.Refunds WHERE ContractID = ? AND " + filter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? PaymentAmounts.safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }
}
