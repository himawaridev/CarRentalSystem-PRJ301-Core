package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentMode;
import com.carrental.model.PaymentStatus;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.PaymentType;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import com.carrental.model.PaymentWebhookResult;
import com.carrental.service.PayOsGateway;
import com.carrental.service.PaymentLinkRequest;
import com.carrental.service.PaymentLinkResponse;
import com.carrental.service.PaymentLinkStatusResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public class PaymentDAO {

    private static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    private static final String METHOD_CASH = "CASH";
    private static final String METHOD_OTHER = "OTHER";

    public PaymentTransaction createPendingPayments(
            Connection conn,
            long contractId,
            String contractCode,
            BigDecimal depositAmount,
            BigDecimal rentalAmount,
            BigDecimal driverFeeAmount,
            PaymentMode paymentMode) throws SQLException {

        BigDecimal amountToPay = safe(depositAmount);
        if (paymentMode == PaymentMode.FULL_PREPAYMENT) {
            amountToPay = amountToPay.add(safe(rentalAmount)).add(safe(driverFeeAmount));
        }

        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Payment amount must be greater than zero.");
        }

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(15);
        long providerOrderCode = generateProviderOrderCode(contractId);
        String providerRef = Long.toString(providerOrderCode);

        PaymentLinkRequest linkRequest = new PaymentLinkRequest();
        linkRequest.setOrderCode(providerOrderCode);
        linkRequest.setContractCode(contractCode);
        linkRequest.setAmount(amountToPay);
        linkRequest.setExpiredAt(expiredAt);

        PaymentLinkResponse paymentLink = createGatewayPaymentLink(linkRequest);

        PaymentTransaction tx = insertPaymentTransaction(
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

        insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.DEPOSIT,
                depositAmount, providerRef, "Required deposit to reserve the car");

        if (paymentMode == PaymentMode.FULL_PREPAYMENT) {
            insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.RENTAL_PREPAID,
                    rentalAmount, providerRef, "Rental fee prepaid at booking time");
            insertPaymentLine(conn, contractId, tx.getPaymentTransactionId(), PaymentType.DRIVER_FEE_PREPAID,
                    driverFeeAmount, providerRef, "Driver fee prepaid at booking time");
        }

        return tx;
    }

    public PaymentTransaction getTransactionByRef(String providerTransactionRef) {
        String sql = "SELECT * FROM dbo.Payment_Transactions WHERE ProviderTransactionRef = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerTransactionRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PaymentTransaction reconcilePendingTransactionWithGateway(String providerTransactionRef) {
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
                    && safe(status.getAmount()).compareTo(safe(tx.getAmount())) == 0
                    && safe(status.getAmountPaid()).compareTo(safe(tx.getAmount())) >= 0
                    && safe(status.getAmountRemaining()).compareTo(BigDecimal.ZERO) == 0;

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

    public PaymentTransaction getLatestPendingTransactionByContractId(long contractId) {
        String sql = "SELECT TOP 1 * FROM dbo.Payment_Transactions "
                + "WHERE ContractID = ? AND Status = N'PENDING' "
                + "ORDER BY CreatedAt DESC, PaymentTransactionID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean confirmPaymentTransaction(String providerTransactionRef, String providerPaymentRef) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = lockTransaction(conn, providerTransactionRef);
                if (tx == null) {
                    conn.rollback();
                    return false;
                }

                if (tx.getStatus() == PaymentStatus.PAID) {
                    conn.commit();
                    return true;
                }

                if (tx.getExpiredAt() != null && tx.getExpiredAt().isBefore(LocalDateTime.now())) {
                    expireTransaction(conn, tx.getPaymentTransactionId(), tx.getContractId());
                    conn.commit();
                    return false;
                }

                markTransactionPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);
                markPaymentLinesPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);

                if (hasPaidDeposit(conn, tx.getPaymentTransactionId())) {
                    reserveContractAfterDeposit(conn, tx.getContractId());
                    markFinalPaidIfPrepaid(conn, tx.getContractId());
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

    private boolean confirmPaidTransactionFromGatewayStatus(String providerTransactionRef, String providerPaymentRef) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PaymentTransaction tx = lockTransaction(conn, providerTransactionRef);
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

                markTransactionPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);
                markPaymentLinesPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);

                if (hasPaidDeposit(conn, tx.getPaymentTransactionId())) {
                    reserveContractAfterDeposit(conn, tx.getContractId());
                    markFinalPaidIfPrepaid(conn, tx.getContractId());
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

    public PaymentWebhookResult confirmPaymentFromGatewayWebhook(
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
                Long webhookEventId = insertWebhookEvent(
                        conn, provider, eventRef, Long.toString(providerOrderCode), rawPayload, signature);
                if (webhookEventId == null) {
                    conn.commit();
                    return PaymentWebhookResult.duplicate("Webhook already processed.");
                }

                PaymentTransaction tx = lockTransactionByProviderOrderCode(conn, provider, providerOrderCode);
                if (tx == null) {
                    markWebhookFailed(conn, webhookEventId, "Payment transaction not found.");
                    conn.commit();
                    return PaymentWebhookResult.failure("Payment transaction not found.");
                }

                if (safe(tx.getAmount()).compareTo(safe(paidAmount)) != 0) {
                    markWebhookFailed(conn, webhookEventId, "Paid amount does not match expected amount.");
                    conn.commit();
                    return PaymentWebhookResult.failure("Paid amount does not match expected amount.");
                }

                if (tx.getStatus() == PaymentStatus.PAID) {
                    markWebhookProcessed(conn, webhookEventId);
                    conn.commit();
                    return PaymentWebhookResult.duplicate("Payment transaction was already paid.");
                }

                if (tx.getStatus() != PaymentStatus.PENDING) {
                    markWebhookFailed(conn, webhookEventId, "Payment transaction is not pending.");
                    conn.commit();
                    return PaymentWebhookResult.failure("Payment transaction is not pending.");
                }

                markTransactionPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);
                markPaymentLinesPaid(conn, tx.getPaymentTransactionId(), providerPaymentRef);

                if (hasPaidDeposit(conn, tx.getPaymentTransactionId())) {
                    reserveContractAfterDeposit(conn, tx.getContractId());
                    markFinalPaidIfPrepaid(conn, tx.getContractId());
                }

                markWebhookProcessed(conn, webhookEventId);
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

    public SettlementResult calculateSettlement(long contractId) {
        SettlementResult result = new SettlementResult();
        result.setContractId(contractId);

        try (Connection conn = DBContext.getConnection()) {
            loadContractAmount(conn, result);

            BigDecimal depositPaid = sumPayments(conn, contractId,
                    "PaymentType = N'DEPOSIT' AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED', N'REFUNDED')");
            BigDecimal refunded = sumRefundedAmount(conn, contractId);
            BigDecimal rentalPaid = sumPayments(conn, contractId,
                    "PaymentType IN (N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID', N'RENTAL_BALANCE') AND PaymentStatus = N'PAID'");
            BigDecimal extraTotal = sumPayments(conn, contractId,
                    "PaymentType = N'EXTRA_CHARGE' AND PaymentStatus NOT IN (N'FAILED', N'EXPIRED')");
            BigDecimal extraPaid = sumPayments(conn, contractId,
                    "PaymentType = N'EXTRA_CHARGE' AND PaymentStatus = N'PAID'");

            BigDecimal depositHeld = maxZero(depositPaid.subtract(refunded));
            BigDecimal unpaidRental = maxZero(result.getExpectedRental().subtract(rentalPaid));
            BigDecimal cashOverRental = maxZero(rentalPaid.subtract(result.getExpectedRental()));
            BigDecimal unsettledExtra = maxZero(extraTotal.subtract(extraPaid).subtract(cashOverRental));
            BigDecimal deduction = min(depositHeld, unsettledExtra);
            BigDecimal amountToCollect = maxZero(unpaidRental.add(unsettledExtra).subtract(deduction));
            BigDecimal refundAmount = maxZero(depositHeld.subtract(deduction));

            result.setDepositPaid(depositPaid);
            result.setRentalPaid(rentalPaid);
            result.setExtraCharge(unsettledExtra);
            result.setDeductionAmount(deduction);
            result.setAmountToCollect(amountToCollect);
            result.setRefundAmount(refundAmount);
            result.setSourcePaymentId(findLatestDepositPaymentId(conn, contractId));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Refund createRefundRequest(long contractId, int approvedByUserId, String reason) {
        return createRefundRequest(contractId, approvedByUserId, reason, RefundMethod.GATEWAY_REFUND);
    }

    public Refund createRefundRequest(
            long contractId,
            int approvedByUserId,
            String reason,
            RefundMethod refundMethod) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Refund pendingRefund = findPendingRefund(conn, contractId);
                if (pendingRefund != null) {
                    conn.commit();
                    return pendingRefund;
                }

                SettlementResult settlement = calculateSettlement(contractId);
                if (settlement.getSourcePaymentId() == null
                        || settlement.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    conn.rollback();
                    return null;
                }

                Refund refund = insertRefund(conn, settlement, approvedByUserId, reason, refundMethod);
                markDepositRefundPending(conn, settlement.getSourcePaymentId());
                moveContractToSettlementPending(conn, contractId);
                conn.commit();
                return refund;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean markRefundCompleted(long refundId, String providerRefundRef) {
        return markRefundCompleted(
                refundId, RefundMethod.MANUAL_BANK_TRANSFER, providerRefundRef, providerRefundRef, null);
    }

    public boolean markRefundCompleted(
            long refundId,
            RefundMethod refundMethod,
            String providerRefundRef,
            String proofOfRefund,
            Integer completedByUserId) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Refund refund = lockRefund(conn, refundId);
                if (refund == null || refund.getStatus() == PaymentStatus.REFUNDED) {
                    conn.rollback();
                    return false;
                }

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

                refund.setRefundMethod(refundMethod);
                refund.setProofOfRefund(proofOfRefund);
                insertRefundPaymentLine(conn, refund, providerRefundRef);
                if (ContractStatus.CANCELLED.equals(getContractStatus(conn, refund.getContractId()))) {
                    updateCancelledContractPaymentsAfterRefund(conn, refund.getContractId());
                } else {
                    updateSourcePaymentAfterRefund(conn, refund);
                }
                tryCompleteSettledContract(conn, refund.getContractId());

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

    public boolean markRefundGatewayFailed(long refundId, String errorMessage) {
        String sql = "UPDATE dbo.Refunds SET RefundMethod = N'GATEWAY_REFUND', ProofOfRefund = ?, "
                + "UpdatedAt = SYSUTCDATETIME() "
                + "WHERE RefundID = ? AND Status = N'REFUND_PENDING'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, errorMessage);
            ps.setLong(2, refundId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PaymentTransaction insertPaymentTransaction(
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

    private void insertPaymentLine(
            Connection conn,
            long contractId,
            long paymentTransactionId,
            PaymentType paymentType,
            BigDecimal amount,
            String transactionRef,
            String note) throws SQLException {

        if (safe(amount).compareTo(BigDecimal.ZERO) <= 0) {
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

    private PaymentTransaction lockTransaction(Connection conn, String providerTransactionRef) throws SQLException {
        String sql = "SELECT * FROM dbo.Payment_Transactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE ProviderTransactionRef = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerTransactionRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        }
        return null;
    }

    private PaymentTransaction lockTransactionByProviderOrderCode(
            Connection conn, String provider, long providerOrderCode) throws SQLException {
        String sql = "SELECT * FROM dbo.Payment_Transactions WITH (UPDLOCK, ROWLOCK) "
                + "WHERE Provider = ? AND ProviderOrderCode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provider);
            ps.setLong(2, providerOrderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        }
        return null;
    }

    private Long insertWebhookEvent(
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

    private void markWebhookProcessed(Connection conn, long webhookEventId) throws SQLException {
        String sql = "UPDATE dbo.Payment_Webhook_Events "
                + "SET ProcessingStatus = N'PROCESSED', ProcessedAt = SYSUTCDATETIME() "
                + "WHERE WebhookEventID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, webhookEventId);
            ps.executeUpdate();
        }
    }

    private void markWebhookFailed(Connection conn, long webhookEventId, String errorMessage) throws SQLException {
        String sql = "UPDATE dbo.Payment_Webhook_Events "
                + "SET ProcessingStatus = N'FAILED', ErrorMessage = ?, ProcessedAt = SYSUTCDATETIME() "
                + "WHERE WebhookEventID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, errorMessage);
            ps.setLong(2, webhookEventId);
            ps.executeUpdate();
        }
    }

    private void markTransactionPaid(Connection conn, long paymentTransactionId, String providerPaymentRef)
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

    private void markPaymentLinesPaid(Connection conn, long paymentTransactionId, String providerPaymentRef)
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

    private boolean hasPaidDeposit(Connection conn, long paymentTransactionId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM dbo.Payments "
                + "WHERE PaymentTransactionID = ? AND PaymentType = N'DEPOSIT' AND PaymentStatus = N'PAID'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentTransactionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void reserveContractAfterDeposit(Connection conn, long contractId) throws SQLException {
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

    private void markFinalPaidIfPrepaid(Connection conn, long contractId) throws SQLException {
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

    private void expireTransaction(Connection conn, long paymentTransactionId, long contractId) throws SQLException {
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

    private void loadContractAmount(Connection conn, SettlementResult result) throws SQLException {
        String sql = "SELECT FinalAmountDue FROM dbo.Contracts WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, result.getContractId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.setExpectedRental(rs.getBigDecimal("FinalAmountDue"));
                }
            }
        }
    }

    private BigDecimal sumPayments(Connection conn, long contractId, String filter) throws SQLException {
        String sql = "SELECT COALESCE(SUM(Amount), 0) FROM dbo.Payments WHERE ContractID = ? AND " + filter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }

    private BigDecimal sumRefundedAmount(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(RefundAmount), 0) FROM dbo.Refunds "
                + "WHERE ContractID = ? AND Status = N'REFUNDED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }

    private Long findLatestDepositPaymentId(Connection conn, long contractId) throws SQLException {
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

    private Refund insertRefund(
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

    private Refund lockRefund(Connection conn, long refundId) throws SQLException {
        String sql = "SELECT * FROM dbo.Refunds WITH (UPDLOCK, ROWLOCK) WHERE RefundID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, refundId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRefund(rs);
            }
        }
    }

    private void insertRefundPaymentLine(Connection conn, Refund refund, String providerRefundRef) throws SQLException {
        if (safe(refund.getRefundAmount()).compareTo(BigDecimal.ZERO) <= 0) {
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

    private String paymentMethodForRefund(RefundMethod refundMethod) {
        if (refundMethod == RefundMethod.CASH_AT_COUNTER) {
            return METHOD_CASH;
        }
        if (refundMethod == RefundMethod.WALLET_CREDIT) {
            return METHOD_OTHER;
        }
        return METHOD_BANK_TRANSFER;
    }

    private void updateSourcePaymentAfterRefund(Connection conn, Refund refund) throws SQLException {
        BigDecimal settledDeposit = safe(refund.getRefundAmount()).add(safe(refund.getDeductionAmount()));
        String newStatus = settledDeposit.compareTo(safe(refund.getDepositAmount())) >= 0
                ? PaymentStatus.REFUNDED.name()
                : PaymentStatus.PARTIALLY_REFUNDED.name();

        String sql = "UPDATE dbo.Payments SET PaymentStatus = ? WHERE PaymentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, refund.getSourcePaymentId());
            ps.executeUpdate();
        }
    }

    private void tryCompleteSettledContract(Connection conn, long contractId) throws SQLException {
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

    private String getContractStatus(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT Status FROM dbo.Contracts WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Status") : null;
            }
        }
    }

    private void markDepositRefundPending(Connection conn, long paymentId) throws SQLException {
        String sql = "UPDATE dbo.Payments SET PaymentStatus = N'REFUND_PENDING' WHERE PaymentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            ps.executeUpdate();
        }
    }

    private void moveContractToSettlementPending(Connection conn, long contractId) throws SQLException {
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

    private void insertStatusHistory(Connection conn, long contractId, String oldStatus, String newStatus, String note)
            throws SQLException {
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

    private PaymentTransaction mapTransaction(ResultSet rs) throws SQLException {
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

    private long generateProviderOrderCode(long contractId) {
        long entropy = System.currentTimeMillis() % 100_000L;
        long random = ThreadLocalRandom.current().nextLong(100L, 999L);
        return (contractId * 100_000_000L) + (entropy * 1_000L) + random;
    }

    private PaymentLinkResponse createGatewayPaymentLink(PaymentLinkRequest request) throws SQLException {
        PayOsGateway gateway = new PayOsGateway();
        try {
            return gateway.createPaymentLink(request);
        } catch (IOException | RuntimeException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new SQLException("Cannot create real payment link from payOS: " + e.getMessage(), e);
        }
    }

    public Refund getPendingRefundByContractId(long contractId) {
        try (Connection conn = DBContext.getConnection()) {
            return findPendingRefund(conn, contractId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean recordRentalBalancePayment(long contractId, BigDecimal amount, int receivedByUserId, String note) {
        if (safe(amount).compareTo(BigDecimal.ZERO) <= 0) {
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

    public BigDecimal calculateCancellationRefundAmount(long contractId) {
        try (Connection conn = DBContext.getConnection()) {
            return calculateCancellationRefundAmount(conn, contractId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public boolean cancelContractWithRefund(long contractId, int changedByUserId, String reason) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String oldStatus = getContractStatusForUpdate(conn, contractId);
                if (!ContractStatus.PENDING_PAYMENT.equals(oldStatus)
                        && !ContractStatus.RESERVED.equals(oldStatus)
                        && !ContractStatus.CONFIRMED.equals(oldStatus)) {
                    conn.rollback();
                    return false;
                }

                Refund pendingRefund = findPendingRefund(conn, contractId);
                if (!ContractStatus.PENDING_PAYMENT.equals(oldStatus) && pendingRefund == null) {
                    BigDecimal refundAmount = calculateCancellationRefundAmount(conn, contractId);
                    Long sourcePaymentId = findLatestRefundablePaymentId(conn, contractId);
                    if (refundAmount.compareTo(BigDecimal.ZERO) > 0 && sourcePaymentId != null) {
                        SettlementResult cancellation = new SettlementResult();
                        cancellation.setContractId(contractId);
                        cancellation.setSourcePaymentId(sourcePaymentId);
                        cancellation.setDepositPaid(refundAmount);
                        cancellation.setDeductionAmount(BigDecimal.ZERO);
                        cancellation.setRefundAmount(refundAmount);
                        insertRefund(conn, cancellation, changedByUserId, reason, RefundMethod.MANUAL_BANK_TRANSFER);
                        markCancellationPaymentsRefundPending(conn, contractId);
                    }
                }

                cancelContract(conn, contractId, oldStatus, changedByUserId, reason);
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

    private Refund findPendingRefund(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT TOP 1 * FROM dbo.Refunds "
                + "WHERE ContractID = ? AND Status = N'REFUND_PENDING' "
                + "ORDER BY RefundID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRefund(rs);
                }
            }
        }
        return null;
    }

    private BigDecimal calculateCancellationRefundAmount(Connection conn, long contractId) throws SQLException {
        BigDecimal refundablePaid = sumPayments(conn, contractId,
                "PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                        + "AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED')");
        BigDecimal alreadyRefunding = sumRefundAmount(conn, contractId,
                "Status IN (N'REFUND_PENDING', N'REFUNDED')");
        return maxZero(refundablePaid.subtract(alreadyRefunding));
    }

    private BigDecimal sumRefundAmount(Connection conn, long contractId, String filter) throws SQLException {
        String sql = "SELECT COALESCE(SUM(RefundAmount), 0) FROM dbo.Refunds WHERE ContractID = ? AND " + filter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }

    private Long findLatestRefundablePaymentId(Connection conn, long contractId) throws SQLException {
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

    private void markCancellationPaymentsRefundPending(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Payments SET PaymentStatus = N'REFUND_PENDING' "
                + "WHERE ContractID = ? "
                + "AND PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                + "AND PaymentStatus = N'PAID'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    private void updateCancelledContractPaymentsAfterRefund(Connection conn, long contractId) throws SQLException {
        String sql = "UPDATE dbo.Payments SET PaymentStatus = N'REFUNDED' "
                + "WHERE ContractID = ? "
                + "AND PaymentType IN (N'DEPOSIT', N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID') "
                + "AND PaymentStatus IN (N'PAID', N'REFUND_PENDING', N'PARTIALLY_REFUNDED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    private String getContractStatusForUpdate(Connection conn, long contractId) throws SQLException {
        String sql = "SELECT Status FROM dbo.Contracts WITH (UPDLOCK, ROWLOCK) WHERE ContractID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Status") : null;
            }
        }
    }

    private void cancelContract(
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

    private Refund mapRefund(ResultSet rs) throws SQLException {
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

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal maxZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }

    private BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) <= 0 ? left : right;
    }
}
