package com.trasua.pattern.strategy.payment;

import com.trasua.entity.PaymentMethod;
import com.trasua.pattern.strategy.discount.MoneyFormat;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY: Ví điện tử MoMo (quét mã QR)
 * ============================================================================
 * Khách mở app MoMo quét mã QR của quán, ghi nội dung là mã đơn. Nhân viên
 * kiểm tra ví và bấm "Xác nhận đã thanh toán".
 *
 * Khi tích hợp cổng thanh toán MoMo thật (tạo link thanh toán, nhận IPN) chỉ
 * cần sửa lớp này - OrderService, PaymentProcessor và giao diện KHÔNG phải đổi.
 */
public class MomoPaymentStrategy implements PaymentStrategy {

    private final String accountName;
    private final String qrImageUrl;
    /** Nội dung đọc từ mã QR MoMo tĩnh của quán (chuẩn VietQR). */
    private final String qrBasePayload;

    public MomoPaymentStrategy(String accountName, String qrImageUrl) {
        this(accountName, qrImageUrl, null);
    }

    public MomoPaymentStrategy(String accountName, String qrImageUrl, String qrBasePayload) {
        this.accountName = accountName;
        this.qrImageUrl = qrImageUrl;
        this.qrBasePayload = qrBasePayload;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.VI_MOMO;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        String info = "Quét mã QR MoMo, chuyển " + MoneyFormat.vnd(request.amount())
                + " tới " + accountName + " - Nội dung: " + request.orderCode();
        return new PaymentResult(false, null, info);
    }

    @Override
    public String getQrImageUrl() {
        return qrImageUrl;
    }

    /** Lấy QR MoMo tĩnh của quán, gắn thêm số tiền + nội dung -> QR động (quét bằng MoMo hoặc app ngân hàng). */
    @Override
    public String buildQrPayload(java.math.BigDecimal amount, String note) {
        if (qrBasePayload == null || !VietQr.isValid(qrBasePayload.trim())) {
            return null;
        }
        return VietQr.withAmount(qrBasePayload.trim(), amount, note);
    }

    @Override
    public String getQrReceiver() {
        return "Ví MoMo · " + accountName;
    }

    public String getAccountName() { return accountName; }
}
