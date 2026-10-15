package com.trasua.pattern.strategy.tier;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: STRATEGY (cách xếp hạng khách hàng)
 * ============================================================================
 * Quán có thể xếp hạng khách theo nhiều tiêu chí khác nhau. Mỗi tiêu chí là một lớp
 * cài đặt interface này; trang Quản lý khách hàng cho phép đổi tiêu chí ngay lúc xem
 * (CustomerTierClassifier là Context).
 */
public interface CustomerTierStrategy {

    CustomerTier classify(CustomerStats stats);

    /** Khoá dùng trên URL, VD ?xepHang=chi-tieu */
    String getKey();

    /** Tên tiêu chí hiển thị. */
    String getName();

    /** Mô tả ngưỡng xếp hạng. */
    String getRule();
}
