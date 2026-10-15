package com.trasua.service;

import com.trasua.entity.User;
import com.trasua.pattern.strategy.tier.CustomerTier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Một dòng trong trang Quản lý khách hàng. */
public class CustomerSummary {

    private final User user;
    private final long orderCount;
    private final long completedCount;
    private final BigDecimal totalSpent;
    private final LocalDateTime lastOrderAt;
    private final CustomerTier tier;

    public CustomerSummary(User user, long orderCount, long completedCount, BigDecimal totalSpent,
                           LocalDateTime lastOrderAt, CustomerTier tier) {
        this.user = user;
        this.orderCount = orderCount;
        this.completedCount = completedCount;
        this.totalSpent = totalSpent;
        this.lastOrderAt = lastOrderAt;
        this.tier = tier;
    }

    public User getUser() { return user; }
    /** Tổng số đơn (không tính đơn đã huỷ). */
    public long getOrderCount() { return orderCount; }
    public long getCompletedCount() { return completedCount; }
    /** Tổng tiền các đơn đã hoàn thành. */
    public BigDecimal getTotalSpent() { return totalSpent; }
    public LocalDateTime getLastOrderAt() { return lastOrderAt; }
    public CustomerTier getTier() { return tier; }

    /** Chữ cái đầu của tên (avatar). */
    public String getInitial() {
        String name = user.getFullName() == null ? user.getUsername() : user.getFullName().trim();
        String[] parts = name.split("\\s+");
        return parts[parts.length - 1].substring(0, 1).toUpperCase();
    }
}
