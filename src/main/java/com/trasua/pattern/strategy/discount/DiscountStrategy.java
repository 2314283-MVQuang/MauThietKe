package com.trasua.pattern.strategy.discount;

/**
 * ============================================================================
 *  STRATEGY PATTERN (họ #1: KHUYẾN MÃI) - vai trò: STRATEGY (giao diện)
 * ============================================================================
 *
 * VẤN ĐỀ:
 *   Quán có nhiều kiểu khuyến mãi: giảm 10%, giảm thẳng 15.000đ, mua 2 tặng 1...
 *   Nếu viết tất cả trong một hàm bằng if/else hoặc switch:
 *       if (type == PHAN_TRAM) {...} else if (type == SO_TIEN) {...} else if ...
 *   thì mỗi lần thêm kiểu khuyến mãi mới phải sửa lại hàm cũ -> dễ sinh lỗi,
 *   vi phạm nguyên lý Open/Closed.
 *
 * GIẢI PHÁP (Strategy):
 *   - Định nghĩa một "họ thuật toán" có chung giao diện DiscountStrategy.
 *   - Mỗi thuật toán giảm giá đóng gói trong MỘT lớp riêng
 *     (PercentageDiscountStrategy, FixedAmountDiscountStrategy, ...).
 *   - Lớp sử dụng (Context = {@link PriceCalculator}) chỉ biết tới giao diện,
 *     có thể ĐỔI thuật toán lúc chạy bằng setDiscountStrategy(...).
 *   - Thêm kiểu khuyến mãi mới = thêm một lớp mới, KHÔNG sửa Context.
 */
public interface DiscountStrategy {

    /**
     * Tính số tiền được giảm cho đơn hàng.
     *
     * @param input thông tin giỏ hàng (tạm tính + giá từng ly)
     * @return số tiền giảm và thông báo cho khách
     */
    DiscountResult calculate(DiscountInput input);

    /** Mô tả ngắn chiến lược, ví dụ "Giảm 10% (tối đa 20.000đ)". */
    String describe();
}
