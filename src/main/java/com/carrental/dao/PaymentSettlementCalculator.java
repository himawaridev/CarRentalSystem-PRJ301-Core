package com.carrental.dao;

import com.carrental.config.DBContext;
import com.carrental.model.SettlementResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class PaymentSettlementCalculator {
    private PaymentSettlementCalculator() {
    }

    static SettlementResult calculate(long contractId) {
        SettlementResult result = new SettlementResult();
        result.setContractId(contractId);

        try (Connection conn = DBContext.getConnection()) {
            loadContractAmount(conn, result);

            BigDecimal depositPaid = sumPayments(conn, contractId,
                    "PaymentType = N'DEPOSIT' AND PaymentStatus = N'PAID'");
            BigDecimal rentalPaid = sumPayments(conn, contractId,
                    "PaymentType IN (N'RENTAL_PREPAID', N'DRIVER_FEE_PREPAID', N'RENTAL_BALANCE') AND PaymentStatus = N'PAID'");
            BigDecimal extraTotal = sumPayments(conn, contractId,
                    "PaymentType = N'EXTRA_CHARGE' AND PaymentStatus NOT IN (N'FAILED', N'EXPIRED')");
            BigDecimal extraPaid = sumPayments(conn, contractId,
                    "PaymentType = N'EXTRA_CHARGE' AND PaymentStatus = N'PAID'");

            BigDecimal unpaidRental = PaymentAmounts.maxZero(result.getExpectedRental().subtract(rentalPaid));
            BigDecimal cashOverRental = PaymentAmounts.maxZero(rentalPaid.subtract(result.getExpectedRental()));
            BigDecimal unsettledExtra = PaymentAmounts.maxZero(extraTotal.subtract(extraPaid).subtract(cashOverRental));
            BigDecimal amountToCollect = PaymentAmounts.maxZero(unpaidRental.add(unsettledExtra));

            result.setDepositPaid(depositPaid);
            result.setRentalPaid(rentalPaid);
            result.setExtraCharge(unsettledExtra);
            result.setAmountToCollect(amountToCollect);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static void loadContractAmount(Connection conn, SettlementResult result) throws SQLException {
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

    private static BigDecimal sumPayments(Connection conn, long contractId, String filter) throws SQLException {
        String sql = "SELECT COALESCE(SUM(Amount), 0) FROM dbo.Payments WHERE ContractID = ? AND " + filter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? PaymentAmounts.safe(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }
}
