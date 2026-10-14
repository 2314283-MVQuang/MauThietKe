package com.trasua.entity;

public enum DrinkSize {
    M("Size M"),
    L("Size L");

    private final String label;

    DrinkSize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
