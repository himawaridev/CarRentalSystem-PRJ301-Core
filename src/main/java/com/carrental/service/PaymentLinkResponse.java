package com.carrental.service;

public class PaymentLinkResponse {
    private String provider;
    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;
    private String rawResponse;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getPaymentLinkId() { return paymentLinkId; }
    public void setPaymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
