package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.PaymentRecord;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.PaymentType;
import com.carrental.service.PaymentLinkRequest;
import com.carrental.service.PaymentLinkResponse;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class PaymentBalanceWorkflow {
    private PaymentBalanceWorkflow() {
    }

    static PaymentTransaction getLatestPendingBalanceTransactionByContractId(long contractId) {
        try (Connection conn = DBContext.getConnection()) {
            PaymentTransaction tx = PaymentTransactionStore.findLatestPendingByPaymentType(
                    conn, contractId, PaymentType.RENTAL_BALANCE);
            if (tx != null && PaymentTransactionStore.isExpired(tx)) {
                PaymentWorkflowUpdates.expireTransaction(conn, tx.getPaymentTransactionId(), contractId);
                return null;
            }
            return tx;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static PaymentTransaction createRentalBalancePaymentLink(
            long contractId,
            String contractCode,
            BigDecimal amount) {
        if (PaymentAmounts.safe(amount).compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction existing = PaymentTransactionStore.findLatestPendingByPaymentType(
                        conn, contractId, PaymentType.RENTAL_BALANCE);
                if (existing != null) {
                    if (!PaymentTransactionStore.isExpired(existing)
                            && PaymentAmounts.safe(existing.getAmount()).compareTo(PaymentAmounts.safe(amount)) == 0) {
                        conn.commit();
                        return existing;
                    }
                    PaymentWorkflowUpdates.expireTransaction(conn, existing.getPaymentTransactionId(), contractId);
                }

                LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(15);
                long providerOrderCode = PaymentOrderCodeGenerator.generate(contractId);
                String providerRef = Long.toString(providerOrderCode);

                PaymentLinkRequest linkRequest = new PaymentLinkRequest();
                linkRequest.setOrderCode(providerOrderCode);
                linkRequest.setContractCode(contractCode);
                linkRequest.setAmount(amount);
                linkRequest.setExpiredAt(expiredAt);

                PaymentLinkResponse paymentLink = PaymentGatewayLinkService.createPaymentLink(linkRequest);
                PaymentTransaction tx = PaymentTransactionStore.insert(
                        conn,
                        contractId,
                        paymentLink.getProvider(),
                        providerRef,
                        providerOrderCode,
                        amount,
                        paymentLink.getQrCode(),
                        paymentLink.getCheckoutUrl(),
                        paymentLink.getQrCode(),
                        paymentLink.getRawResponse(),
                        expiredAt);

                PaymentTransactionStore.insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.RENTAL_BALANCE,
                        amount, providerRef, "Rental/driver balance collected by PayOS QR at settlement");

                conn.commit();
                return tx;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
