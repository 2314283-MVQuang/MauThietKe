package com.trasua.pattern.strategy.payment;

import com.trasua.entity.PaymentMethod;
import com.trasua.pattern.strategy.discount.MoneyFormat;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY: Thanh toán tiền mặt
 * ============================================================================
 *  - Tại quầy: nhân viên nhập tiền khách đưa -> kiểm tra đủ tiền, tính tiền thối.
 *  - Đặt online: chưa thu tiền, khách trả khi nhận hàng.
 */
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.TIEN_MAT;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        if (request.cashReceived() == null) {
            return new PaymentResult(false, null, "Thanh toán tiền mặt khi nhận hàng: " + MoneyFormat.vnd(request.amount()));
        }
        if (request.cashReceived().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Tiền khách đưa (" + MoneyFormat.vnd(request.cashReceived())
                    + ") chưa đủ để thanh toán " + MoneyFormat.vnd(request.amount()));
        }
        BigDecimal change = request.cashReceived().subtract(request.amount());
        return new PaymentResult(true, change,
                "Khách đưa " + MoneyFormat.vnd(request.cashReceived()) + ", thối lại " + MoneyFormat.vnd(change));
    }
}
