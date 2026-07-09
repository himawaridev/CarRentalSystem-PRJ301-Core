package com.carrental.dao;

import com.carrental.model.PaymentMode;
import com.carrental.model.PaymentRecord;
import com.carrental.model.PaymentTransaction;
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
        return DemoPaymentWorkflow.createPendingPayments(
                conn, contractId, contractCode, depositAmount, rentalAmount, driverFeeAmount, paymentMode);
    }

    public PaymentTransaction getTransactionByRef(String providerTransactionRef) {
        return DemoPaymentWorkflow.getTransactionByRef(providerTransactionRef);
    }

    public PaymentTransaction getLatestPendingTransactionByContractId(long contractId) {
        return DemoPaymentWorkflow.getLatestPendingTransactionByContractId(contractId);
    }

    public boolean confirmPaymentTransaction(String providerTransactionRef, String providerPaymentRef) {
        return DemoPaymentWorkflow.confirmPaymentTransaction(providerTransactionRef, providerPaymentRef);
    }

    public SettlementResult calculateSettlement(long contractId) {
        return PaymentSettlementCalculator.calculate(contractId);
    }

    public List<PaymentRecord> getPaymentRecordsByContractId(long contractId) {
        return PaymentBalanceWorkflow.getPaymentRecordsByContractId(contractId);
    }

    public boolean recordRentalBalancePayment(long contractId, BigDecimal amount, int receivedByUserId, String note) {
        return PaymentBalanceWorkflow.recordRentalBalancePayment(contractId, amount, receivedByUserId, note);
    }
}
