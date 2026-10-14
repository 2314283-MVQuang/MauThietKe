package com.trasua.pattern.strategy.discount;

import com.trasua.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: CONTEXT
 * ============================================================================
 * Lớp tính tiền đơn hàng. Nó GIỮ một tham chiếu tới DiscountStrategy và
 * ỦY QUYỀN việc tính giảm giá cho chiến lược đó.
 *
 * Điểm mấu chốt: PriceCalculator KHÔNG biết (và không cần biết) đang dùng
 * chiến lược cụ thể nào. Không có if/else theo loại khuyến mãi ở đây.
 * Muốn đổi cách giảm giá -> gọi setDiscountStrategy(...) với chiến lược khác.
 */
public class PriceCalculator {

    /** Kết quả tính tiền cuối cùng của một đơn. */
    public record PriceBreakdown(BigDecimal subtotal, DiscountResult discount, BigDecimal total) {
    }

    private DiscountStrategy discountStrategy;

    public PriceCalculator() {
        this(new NoDiscountStrategy());
    }

    public PriceCalculator(DiscountStrategy discountStrategy) {
        setDiscountStrategy(discountStrategy);
    }

    /** Đổi chiến lược lúc chạy (runtime). */
    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy == null ? new NoDiscountStrategy() : discountStrategy;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    public PriceBreakdown calculate(List<OrderItem> items) {
        DiscountInput input = DiscountInput.fromItems(items);
        // ===> ỦY QUYỀN cho chiến lược hiện tại <===
        DiscountResult discount = discountStrategy.calculate(input);
        BigDecimal total = input.subtotal().subtract(discount.amount()).max(BigDecimal.ZERO);
        return new PriceBreakdown(input.subtotal(), discount, total);
    }
}
