package com.trasua.pattern.strategy.tier;

import java.math.BigDecimal;

/**
 * STRATEGY PATTERN - CONCRETE STRATEGY: xếp hạng theo TỔNG CHI TIÊU.
 * Vàng: từ 1.000.000đ · Bạc: từ 300.000đ · còn lại: Đồng.
 */
public class SpendingTierStrategy implements CustomerTierStrategy {

    public static final BigDecimal GOLD = BigDecimal.valueOf(1_000_000);
    public static final BigDecimal SILVER = BigDecimal.valueOf(300_000);

    @Override
    public CustomerTier classify(CustomerStats stats) {
        if (stats.totalSpent().compareTo(GOLD) >= 0) {
            return CustomerTier.VANG;
        }
        if (stats.totalSpent().compareTo(SILVER) >= 0) {
            return CustomerTier.BAC;
        }
        return CustomerTier.DONG;
    }

    @Override
    public String getKey() {
        return "chi-tieu";
    }

    @Override
    public String getName() {
        return "Theo tổng chi tiêu";
    }

    @Override
    public String getRule() {
        return "Vàng ≥ 1.000.000đ · Bạc ≥ 300.000đ · Đồng: còn lại";
    }
}
