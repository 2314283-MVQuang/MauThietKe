package com.trasua.pattern.strategy.tier;

/**
 * STRATEGY PATTERN - CONCRETE STRATEGY: xếp hạng theo SỐ ĐƠN đã hoàn thành.
 * Vàng: từ 10 đơn · Bạc: từ 3 đơn · còn lại: Đồng.
 */
public class OrderCountTierStrategy implements CustomerTierStrategy {

    public static final long GOLD = 10;
    public static final long SILVER = 3;

    @Override
    public CustomerTier classify(CustomerStats stats) {
        if (stats.orderCount() >= GOLD) {
            return CustomerTier.VANG;
        }
        if (stats.orderCount() >= SILVER) {
            return CustomerTier.BAC;
        }
        return CustomerTier.DONG;
    }

    @Override
    public String getKey() {
        return "so-don";
    }

    @Override
    public String getName() {
        return "Theo số đơn";
    }

    @Override
    public String getRule() {
        return "Vàng ≥ 10 đơn · Bạc ≥ 3 đơn · Đồng: còn lại";
    }
}
