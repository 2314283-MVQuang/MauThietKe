package com.trasua.pattern.strategy.tier;

/** Hạng khách hàng thân thiết. */
public enum CustomerTier {
    DONG("Đồng"),
    BAC("Bạc"),
    VANG("Vàng");

    private final String label;

    CustomerTier(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
