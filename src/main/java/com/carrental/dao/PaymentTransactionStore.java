package com.carrental.dao;

import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.PaymentType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

final class PaymentTransactionStore {
    private static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";

    private PaymentTransactionStore() {
    }

    static PaymentTransaction insert(
            Connection conn,
            long contractId,
            String provider,
            String providerRef,
            long providerOrderCode,
            BigDecimal amount,
            String qrPayload,
            String providerCheckoutUrl,
            String providerQrCode,
            String metadata,
            LocalDateTime expiredAt) throws SQLException {

        String sql = "INSERT INTO dbo.Payment_Transactions "
                + "(ContractID, Provider, ProviderTransactionRef, ProviderOrderCode, Amount, Status, "
                + "ExpiredAt, QrPayload, ProviderCheckoutUrl, ProviderQrCode, Metadata) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, contractId);
            ps.setString(2, provider);
            ps.setString(3, providerRef);
            ps.setLong(4, providerOrderCode);
            ps.setBigDecimal(5, amount);
            ps.setString(6, PaymentStatus.PENDING.name());
            ps.setTimestamp(7, Timestamp.valueOf(expiredAt));
            ps.setString(8, qrPayload);
            ps.setString(9, providerCheckoutUrl);
            ps.setString(10, providerQrCode);
            ps.setString(11, metadata);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Cannot create payment transaction.");
                }
                PaymentTransaction tx = new PaymentTransaction();
                tx.setPaymentTransactionId(keys.getLong(1));
                tx.setContractId(contractId);
                tx.setProvider(provider);
                tx.setProviderTransactionRef(providerRef);
                tx.setProviderOrderCode(providerOrderCode);
                tx.setAmount(amount);
                tx.setStatus(PaymentStatus.PENDING);
                tx.setQrPayload(qrPayload);
                tx.setProviderCheckoutUrl(providerCheckoutUrl);
                tx.setProviderQrCode(providerQrCode);
                tx.setExpiredAt(expiredAt);
                return tx;
            }
        }
    }

    static void insertPaymentLine(
            Connection conn,
            long contractId,
            long paymentTransactionId,
            PaymentType paymentType,
            BigDecimal amount,
            String transactionRef,
            String note) throws SQLException {

        if (PaymentAmounts.safe(amount).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String sql = "INSERT INTO dbo.Payments "
                + "(ContractID, PaymentTransactionID, PaymentType, Amount, PaymentMethod, PaymentStatus, TransactionRef, Note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setLong(2, paymentTransactionId);
            ps.setString(3, paymentType.name());
            ps.setBigDecimal(4, amount);
            ps.setString(5, METHOD_BANK_TRANSFER);
            ps.setString(6, PaymentStatus.PENDING.name());
            ps.setString(7, transactionRef);
            ps.setString(8, note);
            ps.executeUpdate();
        }
    }

    static PaymentTransaction findLatestPendingByPaymentType(
            Connection conn,
            long contractId,
            PaymentType paymentType) throws SQLException {
        String sql = "SELECT TOP 1 tx.* FROM dbo.Payment_Transactions tx "
                + "WHERE tx.ContractID = ? AND tx.Status = N'PENDING' "
                + "AND EXISTS ("
                + "    SELECT 1 FROM dbo.Payments p "
                + "    WHERE p.PaymentTransactionID = tx.PaymentTransactionID "
                + "    AND p.PaymentType = ? "
                + "    AND p.PaymentStatus = N'PENDING'"
                + ") "
                + "ORDER BY tx.CreatedAt DESC, tx.PaymentTransactionID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setString(2, paymentType.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? PaymentRowMapper.mapTransaction(rs) : null;
            }
        }
    }

    static PaymentTransaction findByProviderRef(Connection conn, String providerTransactionRef) throws SQLException {
        String sql = "SELECT * FROM dbo.Payment_Transactions WHERE ProviderTransactionRef = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerTransactionRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapTransaction(rs);
                }
            }
        }
        return null;
    }

    static PaymentTransaction findLatestPendingByContractId(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT TOP 1 * FROM dbo.Payment_Transactions "
                + "WHERE ContractID = ? AND Status = N'PENDING' "
                + "ORDER BY CreatedAt DESC, PaymentTransactionID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapTransaction(rs);
                }
            }
        }
        return null;
    }

    static boolean isExpired(PaymentTransaction tx) {
        return tx != null
                && tx.getExpiredAt() != null
                && tx.getExpiredAt().isBefore(LocalDateTime.now());
    }

    static PaymentTransaction lockByProviderRef(Connection conn, String providerTransactionRef) throws SQLException {
        String sql = "SELECT * FROM dbo.Payment_Transactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE ProviderTransactionRef = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerTransactionRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapTransaction(rs);
                }
            }
        }
        return null;
    }

    static PaymentTransaction lockByProviderOrderCode(
            Connection conn, String provider, long providerOrderCode) throws SQLException {
        String sql = "SELECT * FROM dbo.Payment_Transactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE Provider = ? AND ProviderOrderCode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provider);
            ps.setLong(2, providerOrderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return PaymentRowMapper.mapTransaction(rs);
                }
            }
        }
        return null;
    }
}
