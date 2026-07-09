package com.carrental.dao;

import com.carrental.model.PaymentRecord;
import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

final class PaymentRowMapper {
    private PaymentRowMapper() {
    }

    static PaymentTransaction mapTransaction(ResultSet rs) throws SQLException {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setPaymentTransactionId(rs.getLong("PaymentTransactionID"));
        tx.setContractId(rs.getLong("ContractID"));
        tx.setProvider(rs.getString("Provider"));
        tx.setProviderTransactionRef(rs.getString("ProviderTransactionRef"));
        long orderCode = rs.getLong("ProviderOrderCode");
        if (!rs.wasNull()) {
            tx.setProviderOrderCode(orderCode);
        }
        tx.setProviderPaymentRef(rs.getString("ProviderPaymentRef"));
        tx.setAmount(rs.getBigDecimal("Amount"));
        tx.setStatus(PaymentStatus.valueOf(rs.getString("Status")));
        tx.setQrPayload(rs.getString("QrPayload"));
        tx.setProviderCheckoutUrl(rs.getString("ProviderCheckoutUrl"));
        tx.setProviderQrCode(rs.getString("ProviderQrCode"));
        Timestamp expired = rs.getTimestamp("ExpiredAt");
        if (expired != null) {
            tx.setExpiredAt(expired.toLocalDateTime());
        }
        Timestamp paid = rs.getTimestamp("PaidAt");
        if (paid != null) {
            tx.setPaidAt(paid.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) {
            tx.setCreatedAt(created.toLocalDateTime());
        }
        return tx;
    }

    static PaymentRecord mapPaymentRecord(ResultSet rs) throws SQLException {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentId(rs.getLong("PaymentID"));
        record.setPaymentType(rs.getString("PaymentType"));
        record.setAmount(rs.getBigDecimal("Amount"));
        record.setPaymentMethod(rs.getString("PaymentMethod"));
        record.setPaymentStatus(rs.getString("PaymentStatus"));
        record.setTransactionRef(rs.getString("TransactionRef"));
        record.setNote(rs.getString("Note"));
        record.setProvider(rs.getString("Provider"));
        record.setProviderTransactionRef(rs.getString("ProviderTransactionRef"));

        Timestamp paid = rs.getTimestamp("PaidAt");
        if (paid != null) {
            record.setPaidAt(paid.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) {
            record.setCreatedAt(created.toLocalDateTime());
        }
        return record;
    }
}
