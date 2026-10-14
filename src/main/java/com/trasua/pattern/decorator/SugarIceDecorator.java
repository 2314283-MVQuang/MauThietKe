package com.trasua.pattern.decorator;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: CONCRETE DECORATOR #3 (tuỳ chỉnh đường / đá)
 * ============================================================================
 * Ví dụ cho thấy Decorator không bắt buộc phải đổi giá: lớp này chỉ bổ sung
 * HÀNH VI mô tả (ghi chú mức đường, đá cho quầy pha chế), còn getCost() giữ
 * nguyên => dùng lại cài đặt mặc định của BeverageDecorator (ủy quyền vào trong).
 */
public class SugarIceDecorator extends BeverageDecorator {

    private final int sugarPercent;
    private final int icePercent;

    public SugarIceDecorator(Beverage wrapped, int sugarPercent, int icePercent) {
        super(wrapped);
        this.sugarPercent = sugarPercent;
        this.icePercent = icePercent;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " [" + getAddition() + "]";
    }

    // getCost(): KHÔNG ghi đè -> giá giữ nguyên như ly bên trong.

    @Override
    public String getAddition() {
        return "Đường " + sugarPercent + "%, Đá " + icePercent + "%";
    }
}
