package com.trasua.entity;

public enum PaymentStatus {
    CHUA_THANH_TOAN("Chưa thanh toán", "warning"),
    DA_THANH_TOAN("Đã thanh toán", "success");

    private final String label;
    private final String badge;

    PaymentStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}
