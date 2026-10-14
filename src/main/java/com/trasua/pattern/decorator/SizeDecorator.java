package com.trasua.pattern.decorator;

import com.trasua.entity.DrinkSize;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: CONCRETE DECORATOR #1 (chọn size)
 * ============================================================================
 * Bọc ly nước để ghi nhận size. Size M không cộng tiền, size L cộng thêm phần
 * chênh lệch (giá L - giá M) lấy từ menu.
 */
public class SizeDecorator extends BeverageDecorator {

    private final DrinkSize size;
    private final BigDecimal extraPrice;

    public SizeDecorator(Beverage wrapped, DrinkSize size, BigDecimal extraPrice) {
        super(wrapped);
        this.size = size;
        // Không cho phép giá âm (nếu nhập giá L < giá M thì coi như 0)
        this.extraPrice = (extraPrice == null || extraPrice.signum() < 0) ? BigDecimal.ZERO : extraPrice;
    }

    @Override
    public String getDescription() {
        // Lấy mô tả của ly bên trong rồi NỐI THÊM thông tin size
        return wrapped.getDescription() + " (" + size.getLabel() + ")";
    }

    @Override
    public BigDecimal getCost() {
        // Lấy giá của ly bên trong rồi CỘNG THÊM phụ thu size
        return wrapped.getCost().add(extraPrice);
    }

    @Override
    public String getAddition() {
        return size.getLabel();
    }
}
