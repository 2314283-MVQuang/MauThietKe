package com.trasua.pattern.strategy.discount;

import com.trasua.entity.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dữ liệu đầu vào cho các chiến lược giảm giá.
 *
 * @param subtotal  tổng tiền trước giảm giá
 * @param cupPrices giá của TỪNG LY (món số lượng 3 sẽ xuất hiện 3 lần) - dùng
 *                  cho khuyến mãi "mua X tặng 1 ly rẻ nhất"
 */
public record DiscountInput(BigDecimal subtotal, List<BigDecimal> cupPrices) {

    public DiscountInput {
        subtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
        cupPrices = cupPrices == null ? List.of() : List.copyOf(cupPrices);
    }

    /** Tạo đầu vào từ danh sách dòng đơn hàng. */
    public static DiscountInput fromItems(List<OrderItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BigDecimal> cups = new ArrayList<>();
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
            cups.addAll(Collections.nCopies(item.getQuantity(), item.getUnitPrice()));
        }
        return new DiscountInput(subtotal, cups);
    }

    public int totalCups() {
        return cupPrices.size();
    }
}
