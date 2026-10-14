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

    public BankTransferPaymentStrategy(String bankName, String accountNumber, String accountName, String qrImageUrl) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.qrImageUrl = qrImageUrl;
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
}
