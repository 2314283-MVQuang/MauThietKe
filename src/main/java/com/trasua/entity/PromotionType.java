package com.trasua.entity;

/** Loại khuyến mãi - mỗi loại ứng với một DiscountStrategy. */
public enum PromotionType {
    PHAN_TRAM("Giảm theo %"),
    SO_TIEN("Giảm số tiền cố định"),
    MUA_X_TANG_1("Mua X ly tặng 1 ly rẻ nhất");

    private final String label;

    PromotionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
