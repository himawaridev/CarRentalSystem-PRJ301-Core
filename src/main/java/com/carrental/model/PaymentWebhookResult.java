package com.carrental.model;

public class PaymentWebhookResult {
    private boolean success;
    private boolean duplicate;
    private String message;

    public static PaymentWebhookResult success(String message) {
        PaymentWebhookResult result = new PaymentWebhookResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    public static PaymentWebhookResult duplicate(String message) {
        PaymentWebhookResult result = success(message);
        result.setDuplicate(true);
        return result;
    }

    public static PaymentWebhookResult failure(String message) {
        PaymentWebhookResult result = new PaymentWebhookResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isDuplicate() { return duplicate; }
    public void setDuplicate(boolean duplicate) { this.duplicate = duplicate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
