package com.trasua.entity;

/** Khách góp ý về khía cạnh nào của món / quán. */
public enum FeedbackTopic {
    HUONG_VI("Hương vị / độ ngọt"),
    TOPPING("Topping"),
    SIZE_GIA("Size & giá"),
    TRINH_BAY("Ly, trình bày, nhiệt độ"),
    PHUC_VU("Phục vụ / giao hàng"),
    KHAC("Khác");

    private final String label;

    FeedbackTopic(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
