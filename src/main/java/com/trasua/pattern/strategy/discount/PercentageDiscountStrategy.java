package com.trasua.pattern.strategy.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY #1: Giảm theo phần trăm
 * ============================================================================
 * Ví dụ mã GIAM10: giảm 10% tổng đơn, tối đa 20.000đ, đơn tối thiểu 50.000đ.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private final BigDecimal percent;
    private final BigDecimal maxDiscount;   // null = không giới hạn
    private final BigDecimal minOrderAmount;

    public PercentageDiscountStrategy(BigDecimal percent, BigDecimal maxDiscount, BigDecimal minOrderAmount) {
        if (percent == null || percent.signum() <= 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm phải trong khoảng (0, 100]");
        }
        this.percent = percent;
        this.maxDiscount = maxDiscount;
        this.minOrderAmount = minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
    }

    @Override
    public DiscountResult calculate(DiscountInput input) {
        if (input.subtotal().compareTo(minOrderAmount) < 0) {
            return DiscountResult.notApplied("Đơn tối thiểu " + MoneyFormat.vnd(minOrderAmount) + " để được giảm "
                    + percent.stripTrailingZeros().toPlainString() + "%");
        }
        // tiền giảm = tạm tính * % / 100, làm tròn tới đồng
        BigDecimal discount = input.subtotal()
                .multiply(percent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        if (maxDiscount != null && maxDiscount.signum() > 0 && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }
        return DiscountResult.applied(discount, describe());
    }

    @Override
    public String describe() {
        String text = "Giảm " + percent.stripTrailingZeros().toPlainString() + "%";
        if (maxDiscount != null && maxDiscount.signum() > 0) {
            text += " (tối đa " + MoneyFormat.vnd(maxDiscount) + ")";
        }
        return text;
    }
}
