package com.trasua.pattern.strategy.payment;

import com.trasua.entity.PaymentMethod;
import com.trasua.pattern.strategy.discount.MoneyFormat;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY: Chuyển khoản ngân hàng (VietQR)
 * ============================================================================
 * Khách quét mã VietQR của tài khoản Vietcombank, ghi nội dung chuyển khoản là
 * mã đơn. Đơn ở trạng thái "chưa thanh toán" cho tới khi nhân viên kiểm tra
 * tài khoản và bấm "Xác nhận đã thanh toán".
 */
public class BankTransferPaymentStrategy implements PaymentStrategy {

    private final String bankName;
    private final String accountNumber;
    private final String accountName;
    private final String qrImageUrl;
    /** Mã BIN ngân hàng theo NAPAS (Vietcombank = 970436). */
    private final String bankBin;

    public BankTransferPaymentStrategy(String bankName, String accountNumber, String accountName, String qrImageUrl) {
        this(bankName, accountNumber, accountName, qrImageUrl, "970436");
    }

    public BankTransferPaymentStrategy(String bankName, String accountNumber, String accountName, String qrImageUrl,
                                       String bankBin) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.qrImageUrl = qrImageUrl;
        this.bankBin = bankBin;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CHUYEN_KHOAN;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        String info = "Quét mã VietQR hoặc chuyển " + MoneyFormat.vnd(request.amount()) + " tới " + bankName
                + " - STK " + accountNumber + " (" + accountName + ")"
                + " - Nội dung: " + request.orderCode();
        return new PaymentResult(false, null, info);
    }

    @Override
    public String getQrImageUrl() {
        return qrImageUrl;
    }

    /** VietQR tới STK của quán, kèm đúng số tiền của đơn và nội dung = mã đơn. */
    @Override
    public String buildQrPayload(java.math.BigDecimal amount, String note) {
        if (bankBin == null || bankBin.isBlank() || accountNumber == null || accountNumber.isBlank()) {
            return null;
        }
        return VietQr.forBankAccount(bankBin.trim(), accountNumber.trim(), amount, note);
    }

    @Override
    public String getQrReceiver() {
        return bankName + " · STK " + accountNumber + " · " + accountName;
    }

    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountName() { return accountName; }
}
