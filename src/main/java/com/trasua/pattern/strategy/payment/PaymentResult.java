package com.trasua.pattern.strategy.payment;

import java.math.BigDecimal;

/**
 * Kết quả thanh toán.
 *
 * @param paid         true nếu đã thu tiền xong
 * @param changeAmount tiền thối lại (chỉ tiền mặt)
 * @param info         thông tin hiển thị cho khách / lưu vào đơn
 */
public record PaymentResult(boolean paid, BigDecimal changeAmount, String info) {
}
