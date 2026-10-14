package com.trasua.entity;

/** Các phương thức thanh toán - mỗi giá trị ứng với một PaymentStrategy. */
public enum PaymentMethod {
    TIEN_MAT("Tiền mặt"),
    CHUYEN_KHOAN("Chuyển khoản ngân hàng"),
    VI_MOMO("Ví MoMo");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
