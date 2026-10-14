package com.trasua.pattern.strategy.discount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 *  STRATEGY PATTERN - CONCRETE STRATEGY #3: Mua X ly tặng 1 ly rẻ nhất
 * ============================================================================
 * Ví dụ mã MUA2TANG1 (X = 2): cứ mỗi nhóm 3 ly thì ly rẻ nhất được miễn phí.
 *   Giỏ có 5 ly  -> 5 / 3 = 1 ly miễn phí
 *   Giỏ có 6 ly  -> 6 / 3 = 2 ly miễn phí (2 ly rẻ nhất)
 *
 * Thuật toán này hoàn toàn khác hai chiến lược trên (cần giá từng ly chứ không
 * chỉ tổng tiền) - nhưng Context vẫn gọi y như nhau: strategy.calculate(input).
 */
public class BuyXGetOneFreeStrategy implements DiscountStrategy {

    private final int buyQuantity;

    public BuyXGetOneFreeStrategy(int buyQuantity) {
        if (buyQuantity < 1) {
            throw new IllegalArgumentException("Số ly phải mua tối thiểu là 1");
        }
        this.buyQuantity = buyQuantity;
    }

    @Override
    public DiscountResult calculate(DiscountInput input) {
        int groupSize = buyQuantity + 1;
        int freeCups = input.totalCups() / groupSize;
        if (freeCups == 0) {
            return DiscountResult.notApplied("Cần ít nhất " + groupSize + " ly để được tặng 1 ly");
        }
        // Sắp xếp giá tăng dần, lấy freeCups ly rẻ nhất
        List<BigDecimal> sorted = new ArrayList<>(input.cupPrices());
        sorted.sort(BigDecimal::compareTo);
        BigDecimal discount = BigDecimal.ZERO;
        for (int i = 0; i < freeCups; i++) {
            discount = discount.add(sorted.get(i));
        }
        return DiscountResult.applied(discount, describe() + " - được tặng " + freeCups + " ly");
    }

    @Override
    public String describe() {
        return "Mua " + buyQuantity + " tặng 1 (ly rẻ nhất)";
    }
}
