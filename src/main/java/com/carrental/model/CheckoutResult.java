package com.carrental.model;

public class CheckoutResult {
    private boolean success;
    private String message;
    private SettlementResult settlement;
    private Refund refund;

    public static CheckoutResult success(String message, SettlementResult settlement, Refund refund) {
        CheckoutResult result = new CheckoutResult();
        result.setSuccess(true);
        result.setMessage(message);
        result.setSettlement(settlement);
        result.setRefund(refund);
        return result;
    }

    public static CheckoutResult failure(String message, SettlementResult settlement, Refund refund) {
        CheckoutResult result = new CheckoutResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setSettlement(settlement);
        result.setRefund(refund);
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public SettlementResult getSettlement() { return settlement; }
    public void setSettlement(SettlementResult settlement) { this.settlement = settlement; }

    public Refund getRefund() { return refund; }
    public void setRefund(Refund refund) { this.refund = refund; }
}
