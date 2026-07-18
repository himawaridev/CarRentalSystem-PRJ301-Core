package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentRecord;
import com.carrental.model.SettlementResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PaymentDAO {

    public void createPendingCashDeposit(Connection conn, long contractId, BigDecimal amount)
            throws SQLException {
        if (safe(amount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Deposit amount must be greater than zero.");
        }

        String sql = "INSERT INTO dbo.Payments "
                + "(ContractID, PaymentType, Amount, PaymentMethod, PaymentStatus, Note) "
                + "VALUES (?, N'DEPOSIT', ?, N'CASH', N'PENDING', N'Waiting for cash deposit at counter')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
        }
    }

    public boolean confirmCashDeposit(long contractId, int receivedByUserId) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String reference = "CASH-DEP-" + contractId + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String paymentSql = "UPDATE dbo.Payments "
                        + "SET PaymentStatus = N'PAID', PaidAt = SYSUTCDATETIME(), "
                        + "ReceivedByUserID = ?, TransactionRef = ?, Note = N'Cash deposit received at counter' "
                        + "WHERE ContractID = ? AND PaymentType = N'DEPOSIT' AND PaymentStatus = N'PENDING'";
                try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                    ps.setInt(1, receivedByUserId);
                    ps.setString(2, reference);
                    ps.setLong(3, contractId);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                String contractSql = "UPDATE dbo.Contracts "
                        + "SET Status = N'RESERVED', DepositPaidAt = SYSUTCDATETIME(), "
                        + "ReviewedByUserID = ?, ReviewedAt = SYSUTCDATETIME(), UpdatedAt = SYSUTCDATETIME() "
                        + "WHERE ContractID = ? AND Status = N'PENDING_PAYMENT'";
                try (PreparedStatement ps = conn.prepareStatement(contractSql)) {
                    ps.setInt(1, receivedByUserId);
                    ps.setLong(2, contractId);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                String historySql = "INSERT INTO dbo.Contract_Status_History "
                        + "(ContractID, OldStatus, NewStatus, ChangedByUserID, Note) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(historySql)) {
                    ps.setLong(1, contractId);
                    ps.setString(2, ContractStatus.PENDING_PAYMENT);
                    ps.setString(3, ContractStatus.RESERVED);
                    ps.setInt(4, receivedByUserId);
                    ps.setString(5, "Staff confirmed cash deposit.");
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public SettlementResult calculateSettlement(long contractId) {
        return PaymentSettlementCalculator.calculate(contractId);
    }

    public List<PaymentRecord> getPaymentRecordsByContractId(long contractId) {
        return PaymentBalanceWorkflow.getPaymentRecordsByContractId(contractId);
    }

    public boolean recordRentalBalancePayment(long contractId, BigDecimal amount, int receivedByUserId, String note) {
        return PaymentBalanceWorkflow.recordRentalBalancePayment(contractId, amount, receivedByUserId, note);
    }

    static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    static BigDecimal maxZero(BigDecimal value) {
        return value.max(BigDecimal.ZERO);
    }
}
