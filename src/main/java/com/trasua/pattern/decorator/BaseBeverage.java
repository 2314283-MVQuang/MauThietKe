package com.trasua.pattern.decorator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: CONCRETE COMPONENT (đối tượng gốc)
 * ============================================================================
 * Ly nước "trơn" chưa thêm gì: chỉ có tên đồ uống và giá gốc (giá size M).
 * Đây là "lõi" nằm trong cùng của chuỗi Decorator.
 */
public class BaseBeverage implements Beverage {

    private final String name;
    private final BigDecimal basePrice;

    public BaseBeverage(String name, BigDecimal basePrice) {
        this.name = Objects.requireNonNull(name, "Tên đồ uống không được rỗng");
        this.basePrice = Objects.requireNonNull(basePrice, "Giá gốc không được rỗng");
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public BigDecimal getCost() {
        return basePrice;
    }
}
