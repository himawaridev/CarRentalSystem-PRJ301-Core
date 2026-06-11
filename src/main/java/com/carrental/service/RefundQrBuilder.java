package com.carrental.service;

import com.carrental.model.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RefundQrBuilder {

    private static final String VIETQR_IMAGE_BASE = "https://img.vietqr.io/image/";

    public String buildRefundQrUrl(User receiver, BigDecimal amount, String addInfo) {
        if (receiver == null || !receiver.hasRefundBankInfo()
                || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        long amountValue = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        String bankCode = encodePath(receiver.getBankCode());
        String accountNumber = encodePath(receiver.getBankAccountNumber());
        String info = encodeQuery(addInfo == null ? "HOAN COC CAR RENTAL" : addInfo);
        String accountName = encodeQuery(receiver.getBankAccountHolder());

        return VIETQR_IMAGE_BASE + bankCode + "-" + accountNumber
                + "-compact2.png?amount=" + amountValue
                + "&addInfo=" + info
                + "&accountName=" + accountName;
    }

    private String encodePath(String value) {
        return URLEncoder.encode(value == null ? "" : value.trim(), StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private String encodeQuery(String value) {
        return URLEncoder.encode(value == null ? "" : value.trim(), StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
