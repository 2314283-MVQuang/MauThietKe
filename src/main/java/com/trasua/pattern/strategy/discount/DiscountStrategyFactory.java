package com.trasua.pattern.strategy.discount;

import com.trasua.entity.Promotion;

/**
 * Chọn chiến lược giảm giá phù hợp từ mã khuyến mãi lưu trong CSDL.
 *
 * Đây là NƠI DUY NHẤT trong hệ thống có switch theo loại khuyến mãi; mọi nơi
 * khác (giỏ hàng, OrderBuilder, PriceCalculator) chỉ làm việc với giao diện
 * DiscountStrategy. Thêm loại mới chỉ cần: 1 lớp Strategy mới + 1 nhánh ở đây.
 */
public final class DiscountStrategyFactory {

    private DiscountStrategyFactory() {
    }

    public static DiscountStrategy fromPromotion(Promotion promotion) {
        if (promotion == null) {
            return new NoDiscountStrategy();
        }
        return switch (promotion.getType()) {
            case PHAN_TRAM -> new PercentageDiscountStrategy(
                    promotion.getValue(), promotion.getMaxDiscount(), promotion.getMinOrderAmount());
            case SO_TIEN -> new FixedAmountDiscountStrategy(
                    promotion.getValue(), promotion.getMinOrderAmount());
            case MUA_X_TANG_1 -> new BuyXGetOneFreeStrategy(
                    promotion.getBuyQuantity() == null ? 2 : promotion.getBuyQuantity());
        };
    }
}
