package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.PaymentRecord;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class PaymentBalanceWorkflow {
    private PaymentBalanceWorkflow() {
    }

    static List<PaymentRecord> getPaymentRecordsByContractId(long contractId) {
        List<PaymentRecord> records = new ArrayList<>();
        String sql = "SELECT p.PaymentID, p.PaymentType, p.Amount, p.PaymentMethod, p.PaymentStatus, "
                + "p.PaidAt, p.TransactionRef, p.Note, p.CreatedAt, "
                + "tx.Provider, tx.ProviderTransactionRef "
                + "FROM dbo.Payments p "
                + "LEFT JOIN dbo.Payment_Transactions tx ON tx.PaymentTransactionID = p.PaymentTransactionID "
                + "WHERE p.ContractID = ? "
                + "ORDER BY p.CreatedAt ASC, p.PaymentID ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(PaymentRowMapper.mapPaymentRecord(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    static boolean recordRentalBalancePayment(
            long contractId,
            BigDecimal amount,
            int receivedByUserId,
            String note) {
        if (PaymentAmounts.safe(amount).compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String transactionRef = "BAL-" + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 12).toUpperCase();
                String sql = "INSERT INTO dbo.Payments "
                        + "(ContractID, PaymentType, Amount, PaymentMethod, PaymentStatus, PaidAt, "
                        + "ReceivedByUserID, TransactionRef, Note) "
                        + "VALUES (?, N'RENTAL_BALANCE', ?, N'CASH', N'PAID', SYSUTCDATETIME(), ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, contractId);
                    ps.setBigDecimal(2, amount);
                    ps.setInt(3, receivedByUserId);
                    ps.setString(4, transactionRef);
                    ps.setString(5, note);
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
}
