package com.trasua.pattern.strategy.discount;

import java.math.BigDecimal;

/**
 * Kết quả do một DiscountStrategy trả về.
 *
 * @param amount  số tiền được giảm (>= 0)
 * @param applied true nếu khuyến mãi được áp dụng
 * @param message thông báo hiển thị cho khách
 */
public record DiscountResult(BigDecimal amount, boolean applied, String message) {

    public static DiscountResult applied(BigDecimal amount, String message) {
        return new DiscountResult(amount, true, message);
    }

    public static DiscountResult notApplied(String reason) {
        return new DiscountResult(BigDecimal.ZERO, false, reason);
    }
}
