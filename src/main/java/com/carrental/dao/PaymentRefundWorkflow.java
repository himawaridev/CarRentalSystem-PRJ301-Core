package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.ContractStatus;
import com.carrental.model.PaymentStatus;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

final class PaymentRefundWorkflow {
    private PaymentRefundWorkflow() {
    }

    static Refund createRefundRequest(long contractId, int approvedByUserId, String reason) {
        return createRefundRequest(contractId, approvedByUserId, reason, RefundMethod.GATEWAY_REFUND);
    }

    static Refund createRefundRequest(
            long contractId,
            int approvedByUserId,
            String reason,
            RefundMethod refundMethod) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Refund pendingRefund = PaymentRefundStore.findPending(conn, contractId);
                if (pendingRefund != null) {
                    conn.commit();
                    return pendingRefund;
                }

                SettlementResult settlement = PaymentSettlementCalculator.calculate(contractId);
                if (settlement.getSourcePaymentId() == null
                        || settlement.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    conn.rollback();
                    return null;
                }

                Refund refund = PaymentRefundStore.insert(conn, settlement, approvedByUserId, reason, refundMethod);
                PaymentWorkflowUpdates.markDepositRefundPending(conn, settlement.getSourcePaymentId());
                PaymentWorkflowUpdates.moveContractToSettlementPending(conn, contractId);
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

    static boolean markRefundCompleted(long refundId, String providerRefundRef) {
        return markRefundCompleted(
                refundId, RefundMethod.MANUAL_BANK_TRANSFER, providerRefundRef, providerRefundRef, null);
    }

    static boolean markRefundCompleted(
            long refundId,
            RefundMethod refundMethod,
            String providerRefundRef,
            String proofOfRefund,
            Integer completedByUserId) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Refund refund = PaymentRefundStore.lock(conn, refundId);
                if (refund == null || refund.getStatus() == PaymentStatus.REFUNDED) {
                    conn.rollback();
                    return false;
                }

                PaymentRefundStore.markCompleted(
                        conn, refundId, refundMethod, providerRefundRef, proofOfRefund, completedByUserId);

                refund.setRefundMethod(refundMethod);
                refund.setProofOfRefund(proofOfRefund);
                PaymentRefundStore.insertRefundPaymentLine(conn, refund, providerRefundRef);
                if (ContractStatus.CANCELLED.equals(PaymentWorkflowUpdates.getContractStatus(conn, refund.getContractId()))) {
                    PaymentWorkflowUpdates.updateCancelledContractPaymentsAfterRefund(conn, refund.getContractId());
                } else {
                    PaymentRefundStore.updateSourcePaymentAfterRefund(conn, refund);
                }
                PaymentWorkflowUpdates.tryCompleteSettledContract(conn, refund.getContractId());

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

    static boolean markRefundGatewayFailed(long refundId, String errorMessage) {
        try (Connection conn = DBContext.getConnection()) {
            return PaymentRefundStore.markGatewayFailed(conn, refundId, errorMessage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    static Refund getPendingRefundByContractId(long contractId) {
        try (Connection conn = DBContext.getConnection()) {
            return PaymentRefundStore.findPending(conn, contractId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Refund getRefundById(long refundId) {
        try (Connection conn = DBContext.getConnection()) {
            return PaymentRefundStore.findById(conn, refundId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static BigDecimal calculateCancellationRefundAmount(long contractId) {
        try (Connection conn = DBContext.getConnection()) {
            return PaymentRefundStore.calculateCancellationRefundAmount(conn, contractId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    static boolean cancelContractWithRefund(long contractId, int changedByUserId, String reason) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String oldStatus = PaymentWorkflowUpdates.getContractStatusForUpdate(conn, contractId);
                if (!ContractStatus.PENDING_PAYMENT.equals(oldStatus)
                        && !ContractStatus.RESERVED.equals(oldStatus)
                        && !ContractStatus.CONFIRMED.equals(oldStatus)) {
                    conn.rollback();
                    return false;
                }

                Refund pendingRefund = PaymentRefundStore.findPending(conn, contractId);
                if (!ContractStatus.PENDING_PAYMENT.equals(oldStatus) && pendingRefund == null) {
                    BigDecimal refundAmount = PaymentRefundStore.calculateCancellationRefundAmount(conn, contractId);
                    Long sourcePaymentId = PaymentRefundStore.findLatestRefundablePaymentId(conn, contractId);
                    if (refundAmount.compareTo(BigDecimal.ZERO) > 0 && sourcePaymentId != null) {
                        SettlementResult cancellation = new SettlementResult();
                        cancellation.setContractId(contractId);
                        cancellation.setSourcePaymentId(sourcePaymentId);
                        cancellation.setDepositPaid(refundAmount);
                        cancellation.setDeductionAmount(BigDecimal.ZERO);
                        cancellation.setRefundAmount(refundAmount);
                        PaymentRefundStore.insert(
                                conn, cancellation, changedByUserId, reason, RefundMethod.MANUAL_BANK_TRANSFER);
                        PaymentWorkflowUpdates.markCancellationPaymentsRefundPending(conn, contractId);
                    }
                }

                PaymentWorkflowUpdates.cancelContract(conn, contractId, oldStatus, changedByUserId, reason);
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
