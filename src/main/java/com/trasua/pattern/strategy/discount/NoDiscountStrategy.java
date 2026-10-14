package com.trasua.pattern.strategy.discount;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY #0: Không khuyến mãi
 * ============================================================================
 * Chiến lược "rỗng" (Null Object). Nhờ nó, Context không bao giờ phải kiểm tra
 * {@code if (strategy == null)} - luôn có một chiến lược hợp lệ để gọi.
 */
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountResult calculate(DiscountInput input) {
        return DiscountResult.notApplied("Không áp dụng khuyến mãi");
    }

    @Override
    public String describe() {
        return "Không khuyến mãi";
    }
}
