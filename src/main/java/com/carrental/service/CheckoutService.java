package com.carrental.service;

import com.carrental.dao.ContractDAO;
import com.carrental.dao.PaymentDAO;
import com.carrental.model.CheckoutResult;
import com.carrental.model.ContractStatus;
import com.carrental.model.Refund;
import com.carrental.model.RefundMethod;
import com.carrental.model.SettlementResult;
import java.math.BigDecimal;
import java.util.UUID;

public class CheckoutService {
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ContractDAO contractDAO = new ContractDAO();

    public CheckoutResult processCheckout(
            long contractId,
            int staffUserId,
            RefundMethod refundMethod,
            String proofOfRefund,
            String reason) {

        SettlementResult settlement = paymentDAO.calculateSettlement(contractId);
        if (settlement.getAmountToCollect().compareTo(BigDecimal.ZERO) > 0) {
            return CheckoutResult.failure(
                    "Can thu du tien con lai truoc khi quyet toan coc.", settlement, null);
        }

        Refund pendingRefund = paymentDAO.getPendingRefundByContractId(contractId);
        if (settlement.getRefundAmount().compareTo(BigDecimal.ZERO) > 0 || pendingRefund != null) {
            Refund refund = pendingRefund != null
                    ? pendingRefund
                    : paymentDAO.createRefundRequest(contractId, staffUserId, reason, refundMethod);
            if (refund == null) {
                return CheckoutResult.failure("Khong the tao yeu cau hoan coc.", settlement, null);
            }

            if (refundMethod == RefundMethod.GATEWAY_REFUND) {
                paymentDAO.markRefundGatewayFailed(
                        refund.getRefundId(),
                        "Gateway refund chua hoan tat hoac bi loi. Vui long chon CASH_AT_COUNTER, "
                                + "MANUAL_BANK_TRANSFER hoac WALLET_CREDIT de xu ly fallback.");
                return CheckoutResult.failure(
                        "Gateway refund chua hoan tat. Hay chon mot phuong thuc fallback.",
                        settlement,
                        refund);
            }

            if (refundMethod == RefundMethod.MANUAL_BANK_TRANSFER
                    && (proofOfRefund == null || proofOfRefund.isBlank())) {
                return CheckoutResult.failure(
                        "Can nhap ma giao dich hoac bang chung chuyen khoan thu cong.",
                        settlement,
                        refund);
            }

            String providerRefundRef = normalizeProof(proofOfRefund, refundMethod);
            boolean completed = paymentDAO.markRefundCompleted(
                    refund.getRefundId(),
                    refundMethod,
                    providerRefundRef,
                    proofOfRefund,
                    staffUserId);
            return completed
                    ? CheckoutResult.success("Da hoan coc va hoan tat hop dong.", settlement, refund)
                    : CheckoutResult.failure("Khong the xac nhan hoan coc.", settlement, refund);
        }

        boolean completed = contractDAO.updateContractStatus(contractId, ContractStatus.COMPLETED, staffUserId);
        return completed
                ? CheckoutResult.success("Hop dong da hoan tat.", settlement, null)
                : CheckoutResult.failure("Khong the hoan tat hop dong.", settlement, null);
    }

    private String normalizeProof(String proofOfRefund, RefundMethod refundMethod) {
        if (proofOfRefund != null && !proofOfRefund.isBlank()) {
            return proofOfRefund.trim();
        }
        return switch (refundMethod) {
            case CASH_AT_COUNTER -> "CASH-" + shortRef();
            case WALLET_CREDIT -> "WALLET-" + shortRef();
            case MANUAL_BANK_TRANSFER -> "BANK-" + shortRef();
            case GATEWAY_REFUND -> "GATEWAY-" + shortRef();
        };
    }

    private String shortRef() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
