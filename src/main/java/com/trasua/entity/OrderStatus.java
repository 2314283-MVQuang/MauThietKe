package com.trasua.entity;

/**
 * Vòng đời đơn hàng:
 * CHO_XAC_NHAN -> DA_XAC_NHAN -> DANG_PHA_CHE -> (DANG_GIAO) -> HOAN_THANH
 * Có thể chuyển sang DA_HUY khi đơn chưa hoàn thành.
 */
public enum OrderStatus {
    CHO_XAC_NHAN("Chờ xác nhận", "warning"),
    DA_XAC_NHAN("Đã xác nhận", "info"),
    DANG_PHA_CHE("Đang pha chế", "primary"),
    DANG_GIAO("Đang giao", "secondary"),
    HOAN_THANH("Hoàn thành", "success"),
    DA_HUY("Đã huỷ", "danger");

    private final String label;
    /** Màu badge Bootstrap để hiển thị. */
    private final String badge;

    OrderStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public boolean isFinished() {
        return this == HOAN_THANH || this == DA_HUY;
    }

    /** Trạng thái kế tiếp theo quy trình (null nếu đã kết thúc). */
    public OrderStatus next(OrderType type) {
        return switch (this) {
            case CHO_XAC_NHAN -> DA_XAC_NHAN;
            case DA_XAC_NHAN -> DANG_PHA_CHE;
            case DANG_PHA_CHE -> type == OrderType.GIAO_HANG ? DANG_GIAO : HOAN_THANH;
            case DANG_GIAO -> HOAN_THANH;
            default -> null;
        };
    }
}
