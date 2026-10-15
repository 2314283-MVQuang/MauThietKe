package com.trasua.pattern.decorator;

import com.trasua.entity.DrinkSize;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: CONCRETE DECORATOR #1 (chọn size)
 * ============================================================================
 * Bọc ly nước để ghi nhận size. Size M không cộng tiền; size khác cộng thêm phần
 * chênh lệch (giá size đó - giá M) lấy từ menu. Size nhỏ hơn M (VD size S) có chênh lệch âm
 * -> ly rẻ hơn. Quản lý thêm size mới cho món = không phải sửa lớp này.
 */
public class SizeDecorator extends BeverageDecorator {

    private final String sizeLabel;
    private final BigDecimal extraPrice;

    public SizeDecorator(Beverage wrapped, DrinkSize size, BigDecimal extraPrice) {
        this(wrapped, size.getLabel(), extraPrice);
    }

    /** @param sizeLabel VD "Size XL"; extraPrice có thể âm với size nhỏ hơn M */
    public SizeDecorator(Beverage wrapped, String sizeLabel, BigDecimal extraPrice) {
        super(wrapped);
        this.sizeLabel = sizeLabel;
        this.extraPrice = extraPrice == null ? BigDecimal.ZERO : extraPrice;
    }

    @Override
    public String getDescription() {
        // Lấy mô tả của ly bên trong rồi NỐI THÊM thông tin size
        return wrapped.getDescription() + " (" + sizeLabel + ")";
    }

    @Override
    public BigDecimal getCost() {
        // Lấy giá của ly bên trong rồi CỘNG THÊM phụ thu size
        return wrapped.getCost().add(extraPrice).max(BigDecimal.ZERO);
    }

    @Override
    public String getAddition() {
        return sizeLabel;
    }
}
