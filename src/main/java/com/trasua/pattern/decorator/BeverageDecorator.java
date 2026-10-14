package com.trasua.pattern.decorator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: DECORATOR (lớp bọc trừu tượng)
 * ============================================================================
 * Hai đặc điểm quan trọng của Decorator:
 *   1. "IS-A":  BeverageDecorator implements Beverage
 *               => lớp bọc dùng được ở mọi chỗ cần một Beverage.
 *   2. "HAS-A": BeverageDecorator chứa một Beverage bên trong (wrapped)
 *               => nó ỦY QUYỀN (delegate) cho đối tượng bên trong rồi thêm
 *                  hành vi của riêng mình trước/sau đó.
 *
 * Mặc định lớp này chỉ chuyển tiếp lời gọi; các lớp con (Concrete Decorator)
 * sẽ ghi đè để cộng thêm giá / nối thêm mô tả.
 */
public abstract class BeverageDecorator implements Beverage {

    /** Đối tượng được bọc - có thể là ly gốc hoặc một decorator khác. */
    protected final Beverage wrapped;

    protected BeverageDecorator(Beverage wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "Decorator phải bọc một Beverage");
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public BigDecimal getCost() {
        return wrapped.getCost();
    }

    /** Lấy đối tượng bên trong - dùng để truy vết chuỗi bọc (xem DecoratorChainTracer). */
    public Beverage getWrapped() {
        return wrapped;
    }

    /** Nhãn ngắn của phần mà lớp bọc này thêm vào, ví dụ "Size L", "Trân châu đen". */
    public abstract String getAddition();
}
