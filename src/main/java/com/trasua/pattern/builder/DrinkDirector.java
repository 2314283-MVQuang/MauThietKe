package com.trasua.pattern.builder;

import com.trasua.entity.DrinkSize;
import com.trasua.entity.OrderItem;
import com.trasua.entity.Product;
import com.trasua.entity.Topping;

import java.util.List;

/**
 * ============================================================================
 *  BUILDER PATTERN - vai trò: DIRECTOR
 * ============================================================================
 * Director biết "công thức" - tức THỨ TỰ và GIÁ TRỊ các bước cần gọi trên
 * Builder - để tạo ra những sản phẩm chuẩn hay dùng. Client không cần nhớ
 * công thức, chỉ cần gọi Director.
 *
 * Trong ứng dụng: các nút "Thêm nhanh" ngoài menu dùng Director để thêm ly
 * theo công thức có sẵn mà không phải mở trang tuỳ chỉnh.
 */
public class DrinkDirector {

    /** Công thức mặc định của quán: size M, 70% đường, 70% đá, không topping. */
    public OrderItem makeDefault(Product product) {
        return OrderItemBuilder.forProduct(product)
                .size(DrinkSize.M)
                .sugar(70)
                .ice(70)
                .quantity(1)
                .build();
    }

    /** Công thức "ít ngọt": size M, 30% đường, 50% đá. */
    public OrderItem makeLessSweet(Product product) {
        return OrderItemBuilder.forProduct(product)
                .size(DrinkSize.M)
                .sugar(30)
                .ice(50)
                .quantity(1)
                .note("Ít ngọt")
                .build();
    }

    /** Công thức "đặc biệt": size L, 50% đường, 70% đá, thêm tối đa 2 topping có sẵn. */
    public OrderItem makeSpecial(Product product, List<Topping> availableToppings) {
        OrderItemBuilder builder = OrderItemBuilder.forProduct(product)
                .size(DrinkSize.L)
                .sugar(50)
                .ice(70)
                .quantity(1);
        availableToppings.stream().limit(2).forEach(builder::addTopping);
        return builder.build();
    }
}
