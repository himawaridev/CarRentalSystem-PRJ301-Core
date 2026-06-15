package com.carrental.dao;

import com.carrental.model.PaymentRecord;
import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
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

    static Refund mapRefund(ResultSet rs) throws SQLException {
        Refund refund = new Refund();
        refund.setRefundId(rs.getLong("RefundID"));
        refund.setContractId(rs.getLong("ContractID"));
        refund.setSourcePaymentId(rs.getLong("SourcePaymentID"));
        refund.setDepositAmount(rs.getBigDecimal("DepositAmount"));
        refund.setDeductionAmount(rs.getBigDecimal("DeductionAmount"));
        refund.setRefundAmount(rs.getBigDecimal("RefundAmount"));
        refund.setReason(rs.getString("Reason"));
        String refundMethod = rs.getString("RefundMethod");
        if (refundMethod != null) {
            refund.setRefundMethod(RefundMethod.valueOf(refundMethod));
        }
        refund.setProofOfRefund(rs.getString("ProofOfRefund"));
        refund.setStatus(PaymentStatus.valueOf(rs.getString("Status")));
        int approvedBy = rs.getInt("ApprovedByUserID");
        if (!rs.wasNull()) {
            refund.setApprovedByUserId(approvedBy);
        }
        int completedBy = rs.getInt("CompletedByUserID");
        if (!rs.wasNull()) {
            refund.setCompletedByUserId(completedBy);
        }
        refund.setProviderRefundRef(rs.getString("ProviderRefundRef"));
        Timestamp completedAt = rs.getTimestamp("CompletedAt");
        if (completedAt != null) {
            refund.setCompletedAt(completedAt.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) {
            refund.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("UpdatedAt");
        if (updated != null) {
            refund.setUpdatedAt(updated.toLocalDateTime());
        }
        return refund;
    }
}
