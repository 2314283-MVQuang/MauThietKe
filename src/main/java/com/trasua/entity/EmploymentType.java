package com.trasua.entity;

/** Loại hợp đồng của nhân viên - quyết định cách tính lương (xem SalaryStrategyFactory). */
public enum EmploymentType {
    PART_TIME("Bán thời gian"),
    FULL_TIME("Toàn thời gian");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
