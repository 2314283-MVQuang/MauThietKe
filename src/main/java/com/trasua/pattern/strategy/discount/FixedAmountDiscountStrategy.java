package com.trasua.pattern.strategy.discount;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY #2: Giảm số tiền cố định
 * ============================================================================
 * Ví dụ mã GIAM15K: giảm thẳng 15.000đ cho đơn từ 80.000đ.
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    private final BigDecimal amount;
    private final BigDecimal minOrderAmount;

    public FixedAmountDiscountStrategy(BigDecimal amount, BigDecimal minOrderAmount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Số tiền giảm phải lớn hơn 0");
        }
        this.amount = amount;
        this.minOrderAmount = minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
    }

    @Override
    public DiscountResult calculate(DiscountInput input) {
        if (input.subtotal().compareTo(minOrderAmount) < 0) {
            return DiscountResult.notApplied("Đơn tối thiểu " + MoneyFormat.vnd(minOrderAmount)
                    + " để được giảm " + MoneyFormat.vnd(amount));
        }
        // Không giảm quá giá trị đơn
        BigDecimal discount = amount.min(input.subtotal());
        return DiscountResult.applied(discount, describe());
    }

    @Override
    public String describe() {
        return "Giảm " + MoneyFormat.vnd(amount);
    }
}
