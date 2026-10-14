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

    public MomoPaymentStrategy(String accountName, String qrImageUrl) {
        this.accountName = accountName;
        this.qrImageUrl = qrImageUrl;
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
}
