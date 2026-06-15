package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.PaymentMode;
import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.PaymentType;
import com.carrental.model.PaymentWebhookResult;
import com.carrental.service.PayOsGateway;
import com.carrental.service.PaymentLinkRequest;
import com.carrental.service.PaymentLinkResponse;
import com.carrental.service.PaymentLinkStatusResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

final class PaymentGatewayWorkflow {
    private PaymentGatewayWorkflow() {
    }

    static PaymentTransaction createPendingPayments(
            Connection conn,
            long contractId,
            String contractCode,
            BigDecimal depositAmount,
            BigDecimal rentalAmount,
            BigDecimal driverFeeAmount,
            PaymentMode paymentMode) throws SQLException {

        BigDecimal amountToPay = PaymentAmounts.safe(depositAmount);
        if (paymentMode == PaymentMode.FULL_PREPAYMENT) {
            amountToPay = amountToPay.add(PaymentAmounts.safe(rentalAmount)).add(PaymentAmounts.safe(driverFeeAmount));
        }

        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Payment amount must be greater than zero.");
        }

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(15);
        long providerOrderCode = PaymentOrderCodeGenerator.generate(contractId);
        String providerRef = Long.toString(providerOrderCode);

        PaymentLinkRequest linkRequest = new PaymentLinkRequest();
        linkRequest.setOrderCode(providerOrderCode);
        linkRequest.setContractCode(contractCode);
        linkRequest.setAmount(amountToPay);
        linkRequest.setExpiredAt(expiredAt);

        PaymentLinkResponse paymentLink = PaymentGatewayLinkService.createPaymentLink(linkRequest);

        PaymentTransaction tx = PaymentTransactionStore.insert(
                conn,
                contractId,
                paymentLink.getProvider(),
                providerRef,
                providerOrderCode,
                amountToPay,
                paymentLink.getQrCode(),
                paymentLink.getCheckoutUrl(),
                paymentLink.getQrCode(),
                paymentLink.getRawResponse(),
                expiredAt);

        PaymentTransactionStore.insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.DEPOSIT,
                depositAmount, providerRef, "Required deposit to reserve the car");

        if (paymentMode == PaymentMode.FULL_PREPAYMENT) {
            PaymentTransactionStore.insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.RENTAL_PREPAID,
                    rentalAmount, providerRef, "Rental fee prepaid at booking time");
            PaymentTransactionStore.insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.DRIVER_FEE_PREPAID,
                    driverFeeAmount, providerRef, "Driver fee prepaid at booking time");
        }

        return tx;
    }

    static PaymentTransaction getTransactionByRef(String providerTransactionRef) {
        try (Connection conn = DBContext.getConnection()) {
            return PaymentTransactionStore.findByProviderRef(conn, providerTransactionRef);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static PaymentTransaction reconcilePendingTransactionWithGateway(String providerTransactionRef) {
        PaymentTransaction tx = getTransactionByRef(providerTransactionRef);
        if (tx == null || tx.getStatus() != PaymentStatus.PENDING
                || tx.getProviderOrderCode() == null
                || !PayOsGateway.PROVIDER.equalsIgnoreCase(tx.getProvider())) {
            return tx;
        }

        try {
            PayOsGateway gateway = new PayOsGateway();
            PaymentLinkStatusResponse status = gateway.getPaymentLinkInformation(tx.getProviderOrderCode());
            boolean paidAtGateway = "PAID".equalsIgnoreCase(status.getStatus())
                    && PaymentAmounts.safe(status.getAmount()).compareTo(PaymentAmounts.safe(tx.getAmount())) == 0
                    && PaymentAmounts.safe(status.getAmountPaid()).compareTo(PaymentAmounts.safe(tx.getAmount())) >= 0
                    && PaymentAmounts.safe(status.getAmountRemaining()).compareTo(BigDecimal.ZERO) == 0;

            if (paidAtGateway) {
                String providerPaymentRef = status.getProviderPaymentRef();
                if (providerPaymentRef == null || providerPaymentRef.isBlank()) {
                    providerPaymentRef = PayOsGateway.PROVIDER + "-" + tx.getProviderOrderCode();
                }
                confirmPaidTransactionFromGatewayStatus(providerTransactionRef, providerPaymentRef);
                return getTransactionByRef(providerTransactionRef);
            }
        } catch (IOException | RuntimeException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            e.printStackTrace();
        }

        return tx;
    }

    static PaymentTransaction getLatestPendingTransactionByContractId(long contractId) {
        try (Connection conn = DBContext.getConnection()) {
            return PaymentTransactionStore.findLatestPendingByContractId(conn, contractId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean confirmPaymentTransaction(String providerTransactionRef, String providerPaymentRef) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = PaymentTransactionStore.lockByProviderRef(conn, providerTransactionRef);
                if (tx == null) {
                    conn.rollback();
                    return false;
                }

                if (tx.getStatus() == PaymentStatus.PAID) {
                    conn.commit();
                    return true;
                }

                if (tx.getExpiredAt() != null && tx.getExpiredAt().isBefore(LocalDateTime.now())) {
                    PaymentWorkflowUpdates.expireTransaction(conn, tx.getPaymentTransactionId(), tx.getContractId());
                    conn.commit();
                    return false;
                }

                applyPaidTransaction(conn, tx, providerPaymentRef);
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

    static PaymentWebhookResult confirmPaymentFromGatewayWebhook(
            String provider,
            long providerOrderCode,
            BigDecimal paidAmount,
            String providerPaymentRef,
            String rawPayload,
            String signature) {

        String eventRef = providerPaymentRef == null || providerPaymentRef.isBlank()
                ? provider + "-" + providerOrderCode
                : providerPaymentRef;

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long webhookEventId = PaymentWebhookEventStore.insert(
                        conn, provider, eventRef, Long.toString(providerOrderCode), rawPayload, signature);
                if (webhookEventId == null) {
                    conn.commit();
                    return PaymentWebhookResult.duplicate("Webhook already processed.");
                }

                PaymentTransaction tx = PaymentTransactionStore.lockByProviderOrderCode(conn, provider, providerOrderCode);
                if (tx == null) {
                    PaymentWebhookEventStore.markFailed(conn, webhookEventId, "Payment transaction not found.");
                    conn.commit();
                    return PaymentWebhookResult.failure("Payment transaction not found.");
                }

                if (PaymentAmounts.safe(tx.getAmount()).compareTo(PaymentAmounts.safe(paidAmount)) != 0) {
                    PaymentWebhookEventStore.markFailed(conn, webhookEventId, "Paid amount does not match expected amount.");
                    conn.commit();
                    return PaymentWebhookResult.failure("Paid amount does not match expected amount.");
                }

                if (tx.getStatus() == PaymentStatus.PAID) {
                    PaymentWebhookEventStore.markProcessed(conn, webhookEventId);
                    conn.commit();
                    return PaymentWebhookResult.duplicate("Payment transaction was already paid.");
                }

                if (tx.getStatus() != PaymentStatus.PENDING) {
                    PaymentWebhookEventStore.markFailed(conn, webhookEventId, "Payment transaction is not pending.");
                    conn.commit();
                    return PaymentWebhookResult.failure("Payment transaction is not pending.");
                }

                applyPaidTransaction(conn, tx, providerPaymentRef);
                PaymentWebhookEventStore.markProcessed(conn, webhookEventId);
                conn.commit();
                return PaymentWebhookResult.success("Payment confirmed.");
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return PaymentWebhookResult.failure("Cannot process payment webhook.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return PaymentWebhookResult.failure("Cannot open database connection.");
        }
    }

    private static boolean confirmPaidTransactionFromGatewayStatus(
            String providerTransactionRef,
            String providerPaymentRef) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = PaymentTransactionStore.lockByProviderRef(conn, providerTransactionRef);
                if (tx == null) {
                    conn.rollback();
                    return false;
                }

                if (tx.getStatus() == PaymentStatus.PAID) {
                    conn.commit();
                    return true;
                }

                if (tx.getStatus() != PaymentStatus.PENDING) {
                    conn.rollback();
                    return false;
                }

                applyPaidTransaction(conn, tx, providerPaymentRef);
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

    private static void applyPaidTransaction(
            Connection conn,
            PaymentTransaction tx,
            String providerPaymentRef) throws SQLException {
        PaymentWorkflowUpdates.markTransactionPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);
        PaymentWorkflowUpdates.markPaymentLinesPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);

        if (PaymentWorkflowUpdates.hasPaidDeposit(conn, tx.getPaymentTransactionId())) {
            PaymentWorkflowUpdates.reserveContractAfterDeposit(conn, tx.getContractId());
        }
        PaymentWorkflowUpdates.markFinalPaidIfPrepaid(conn, tx.getContractId());
    }
}
