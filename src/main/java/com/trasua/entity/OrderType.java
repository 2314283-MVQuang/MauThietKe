package com.trasua.entity;

public enum OrderType {
    TAI_QUAN("Uống tại quán"),
    MANG_DI("Mang đi"),
    GIAO_HANG("Giao hàng");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
