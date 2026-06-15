package com.carrental.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class PaymentWebhookEventStore {
    private PaymentWebhookEventStore() {
    }

    static Long insert(
            Connection conn,
            String provider,
            String eventRef,
            String providerTransactionRef,
            String payload,
            String signature) throws SQLException {

        String sql = "INSERT INTO dbo.Payment_Webhook_Events "
                + "(Provider, EventRef, ProviderTransactionRef, Payload, Signature, ProcessingStatus) "
                + "VALUES (?, ?, ?, ?, ?, N'RECEIVED')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, provider);
            ps.setString(2, eventRef);
            ps.setString(3, providerTransactionRef);
            ps.setString(4, payload);
            ps.setString(5, signature);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : null;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627) {
                return null;
            }
            throw e;
        }
    }

    static void markProcessed(Connection conn, long webhookEventId) throws SQLException {
        String sql = "UPDATE dbo.Payment_Webhook_Events "
                + "SET ProcessingStatus = N'PROCESSED', ProcessedAt = SYSUTCDATETIME() "
                + "WHERE WebhookEventID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, webhookEventId);
            ps.executeUpdate();
        }
    }

    static void markFailed(Connection conn, long webhookEventId, String errorMessage) throws SQLException {
        String sql = "UPDATE dbo.Payment_Webhook_Events "
                + "SET ProcessingStatus = N'FAILED', ErrorMessage = ?, ProcessedAt = SYSUTCDATETIME() "
                + "WHERE WebhookEventID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, errorMessage);
            ps.setLong(2, webhookEventId);
            ps.executeUpdate();
        }
    }
}
