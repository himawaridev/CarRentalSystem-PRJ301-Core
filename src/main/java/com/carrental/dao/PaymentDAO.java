package com.carrental.dao;

import com.carrental.model.PaymentMode;
import com.carrental.model.PaymentRecord;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.PaymentWebhookResult;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PaymentDAO {

    public PaymentTransaction createPendingPayments(
            Connection conn,
            long contractId,
            String contractCode,
            BigDecimal depositAmount,
            BigDecimal rentalAmount,
            BigDecimal driverFeeAmount,
            PaymentMode paymentMode) throws SQLException {
        return PaymentGatewayWorkflow.createPendingPayments(
                conn, contractId, contractCode, depositAmount, rentalAmount, driverFeeAmount, paymentMode);
    }

    public PaymentTransaction getTransactionByRef(String providerTransactionRef) {
        return PaymentGatewayWorkflow.getTransactionByRef(providerTransactionRef);
    }

    public PaymentTransaction reconcilePendingTransactionWithGateway(String providerTransactionRef) {
        return PaymentGatewayWorkflow.reconcilePendingTransactionWithGateway(providerTransactionRef);
    }

    public PaymentTransaction getLatestPendingTransactionByContractId(long contractId) {
        return PaymentGatewayWorkflow.getLatestPendingTransactionByContractId(contractId);
    }

    public boolean confirmPaymentTransaction(String providerTransactionRef, String providerPaymentRef) {
        return PaymentGatewayWorkflow.confirmPaymentTransaction(providerTransactionRef, providerPaymentRef);
    }

    public PaymentWebhookResult confirmPaymentFromGatewayWebhook(
            String provider,
            long providerOrderCode,
            BigDecimal paidAmount,
            String providerPaymentRef,
            String rawPayload,
            String signature) {
        return PaymentGatewayWorkflow.confirmPaymentFromGatewayWebhook(
                provider, providerOrderCode, paidAmount, providerPaymentRef, rawPayload, signature);
    }

    public SettlementResult calculateSettlement(long contractId) {
        return PaymentSettlementCalculator.calculate(contractId);
    }

    public Refund createRefundRequest(long contractId, int approvedByUserId, String reason) {
        return PaymentRefundWorkflow.createRefundRequest(contractId, approvedByUserId, reason);
    }

    public Refund createRefundRequest(
            long contractId,
            int approvedByUserId,
            String reason,
            RefundMethod refundMethod) {
        return PaymentRefundWorkflow.createRefundRequest(contractId, approvedByUserId, reason, refundMethod);
    }

    public boolean markRefundCompleted(long refundId, String providerRefundRef) {
        return PaymentRefundWorkflow.markRefundCompleted(refundId, providerRefundRef);
    }

    public boolean markRefundCompleted(
            long refundId,
            RefundMethod refundMethod,
            String providerRefundRef,
            String proofOfRefund,
            Integer completedByUserId) {
        return PaymentRefundWorkflow.markRefundCompleted(
                refundId, refundMethod, providerRefundRef, proofOfRefund, completedByUserId);
    }

    public boolean markRefundGatewayFailed(long refundId, String errorMessage) {
        return PaymentRefundWorkflow.markRefundGatewayFailed(refundId, errorMessage);
    }

    public Refund getPendingRefundByContractId(long contractId) {
        return PaymentRefundWorkflow.getPendingRefundByContractId(contractId);
    }

    public PaymentTransaction getLatestPendingBalanceTransactionByContractId(long contractId) {
        return PaymentBalanceWorkflow.getLatestPendingBalanceTransactionByContractId(contractId);
    }

    public PaymentTransaction createRentalBalancePaymentLink(
            long contractId,
            String contractCode,
            BigDecimal amount) {
        return PaymentBalanceWorkflow.createRentalBalancePaymentLink(contractId, contractCode, amount);
    }

    public List<PaymentRecord> getPaymentRecordsByContractId(long contractId) {
        return PaymentBalanceWorkflow.getPaymentRecordsByContractId(contractId);
    }

    public Refund getRefundById(long refundId) {
        return PaymentRefundWorkflow.getRefundById(refundId);
    }

    public boolean recordRentalBalancePayment(long contractId, BigDecimal amount, int receivedByUserId, String note) {
        return PaymentBalanceWorkflow.recordRentalBalancePayment(contractId, amount, receivedByUserId, note);
    }

    public BigDecimal calculateCancellationRefundAmount(long contractId) {
        return PaymentRefundWorkflow.calculateCancellationRefundAmount(contractId);
    }

    public boolean cancelContractWithRefund(long contractId, int changedByUserId, String reason) {
        return PaymentRefundWorkflow.cancelContractWithRefund(contractId, changedByUserId, reason);
    }
}
