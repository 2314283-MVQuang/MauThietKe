package com.trasua.pattern.strategy.tier;

import java.math.BigDecimal;

/**
 * Số liệu mua hàng của 1 khách - đầu vào cho chiến lược xếp hạng.
 *
 * @param orderCount số đơn đã HOÀN THÀNH
 * @param totalSpent tổng tiền các đơn đã hoàn thành
 */
public record CustomerStats(long orderCount, BigDecimal totalSpent) {

    public CustomerStats {
        totalSpent = totalSpent == null ? BigDecimal.ZERO : totalSpent;
    }
}
