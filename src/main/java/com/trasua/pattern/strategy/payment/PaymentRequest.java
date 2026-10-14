package com.trasua.pattern.strategy.payment;

import java.math.BigDecimal;

/**
 * Yêu cầu thanh toán.
 *
 * @param orderCode    mã đơn hàng
 * @param amount       số tiền phải trả
 * @param cashReceived tiền mặt khách đưa (null nếu chưa thu - ví dụ giao hàng COD)
 */
public record PaymentRequest(String orderCode, BigDecimal amount, BigDecimal cashReceived) {
}
