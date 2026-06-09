# Payment Webhook & Refund Design

## Payment Flow

1. Customer creates a booking and chooses deposit-only or full prepayment.
2. Backend creates `Contracts`, `Contract_Details`, payment lines, then calls payOS to create a real payment link.
3. `Payment_Transactions` stores:
   - `Provider = PAYOS`
   - `ProviderOrderCode`
   - `ProviderTransactionRef`
   - `ProviderCheckoutUrl`
   - `QrPayload` / `ProviderQrCode`
4. Customer scans QR or opens checkout URL.
5. payOS sends webhook to `/payment/webhook/payos`.
6. Backend verifies HMAC-SHA256 signature on `data`.
7. Backend inserts `Payment_Webhook_Events` with unique `(Provider, EventRef)`.
8. Backend locks the payment transaction, validates amount, marks payment lines as `PAID`, and moves contract from `PENDING_PAYMENT` to `RESERVED`.

## Refund Flow

`processCheckout(contractId)` calculates:

- Paid deposit.
- Non-refundable revenue: rental fee and driver fee.
- Extra charges after return.
- Deposit deduction.
- Final refundable deposit amount.

Refund methods:

- `GATEWAY_REFUND`: preferred, but if gateway fails the refund remains pending for fallback.
- `CASH_AT_COUNTER`: staff confirms cash payout at the counter.
- `MANUAL_BANK_TRANSFER`: accounting transfers manually and stores transaction proof.
- `WALLET_CREDIT`: refund is credited to internal wallet/credit.

## Edge Cases

- Duplicate webhook: ignored by unique `Payment_Webhook_Events` and locked transaction status.
- Webhook arrives before customer returns from checkout: webhook is source of truth; return URL only redirects UI.
- Webhook arrives after payment expiration: payment is not auto-reserved if transaction is no longer pending; staff reconciles manually.
- Amount mismatch: webhook is logged as failed and does not update contract/payment.
- Invalid signature: request is rejected with 401.
- Missing webhook: reconciliation job should query payOS by `ProviderOrderCode` for pending transactions older than a threshold.
- Gateway refund fails: keep `Refunds.Status = REFUND_PENDING`, store failure in `ProofOfRefund`, and allow fallback method.
- Manual refund proof missing: staff should provide receipt/transaction reference before confirming refund.
