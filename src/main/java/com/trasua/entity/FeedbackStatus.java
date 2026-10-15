package com.trasua.entity;

/** Tiến trình xử lý góp ý: khách nhìn thấy quán đã làm gì với góp ý của mình. */
public enum FeedbackStatus {
    MOI("Mới gửi", "warning"),
    DA_XEM("Quán đã xem", "info"),
    DANG_CAI_THIEN("Đang cải thiện", "primary"),
    DA_CAI_THIEN("Đã cải thiện", "success");

    private final String label;
    private final String badge;

    FeedbackStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() { return label; }
    public String getBadge() { return badge; }
}
