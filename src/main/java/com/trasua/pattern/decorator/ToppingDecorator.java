package com.trasua.pattern.decorator;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: CONCRETE DECORATOR #2 (thêm topping)
 * ============================================================================
 * Mỗi topping khách chọn sẽ bọc thêm MỘT lớp ToppingDecorator.
 * Chọn 3 topping => ly gốc được bọc 3 lần, giá tự động cộng dồn.
 *
 * Vì danh sách topping do quản lý thêm/sửa trong CSDL (bảng toppings), ta
 * dùng một lớp ToppingDecorator nhận tên + giá topping làm tham số, thay vì
 * viết cứng mỗi topping một lớp (TranChauDecorator, PuddingDecorator...).
 * Nhờ vậy quản lý thêm topping mới KHÔNG cần sửa code (nguyên lý Open/Closed).
 */
public class ToppingDecorator extends BeverageDecorator {

    private final String toppingName;
    private final BigDecimal toppingPrice;

    public ToppingDecorator(Beverage wrapped, String toppingName, BigDecimal toppingPrice) {
        super(wrapped);
        this.toppingName = toppingName;
        this.toppingPrice = toppingPrice == null ? BigDecimal.ZERO : toppingPrice;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + " + toppingName;
    }

    @Override
    public BigDecimal getCost() {
        return wrapped.getCost().add(toppingPrice);
    }

    @Override
    public String getAddition() {
        return toppingName;
    }

    public String getToppingName() {
        return toppingName;
    }
}
