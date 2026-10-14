package com.trasua.entity;

/** Ba vai trò trong hệ thống - dùng cho phân quyền Spring Security. */
public enum Role {
    ADMIN("Quản lý"),
    STAFF("Nhân viên"),
    CUSTOMER("Khách hàng");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
