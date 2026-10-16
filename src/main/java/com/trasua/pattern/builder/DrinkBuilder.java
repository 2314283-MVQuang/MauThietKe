package com.trasua.pattern.builder;

import com.trasua.entity.DrinkSize;
import com.trasua.entity.OrderItem;
import com.trasua.entity.Topping;

/**
 * ============================================================================
 *  BUILDER PATTERN - vai trò: BUILDER (interface)
 * ============================================================================
 * Khai báo CÁC BƯỚC để tạo ra một ly nước (Product = OrderItem), nhưng
 * KHÔNG nói mỗi bước làm cụ thể ra sao - phần đó do ConcreteBuilder cài đặt.
 *
 *   Product         : OrderItem          (ly nước hoàn chỉnh)
 *   Builder         : DrinkBuilder       (interface này - khai báo các bước)
 *   ConcreteBuilder : OrderItemBuilder   (cài đặt các bước + build())
 *   Director        : DrinkDirector      (gọi các bước theo công thức có sẵn)
 *
 * Director chỉ làm việc với interface này, nên muốn có cách dựng ly khác
 * (VD dựng phiếu công thức cho quầy pha chế) chỉ cần viết thêm một
 * ConcreteBuilder mới, không phải sửa Director.
 *
 * Mỗi bước trả về chính Builder để gọi nối liền (method chaining):
 *     builder.size("L").sugar(50).ice(70).quantity(2).build();
 */
public interface DrinkBuilder {

    /** Bước: chọn size theo mã (M, L hoặc size quản lý thêm như S, XL). */
    DrinkBuilder size(String sizeCode);

    /** Bước: chọn size theo hằng số có sẵn (M / L). */
    default DrinkBuilder size(DrinkSize size) {
        return size(size == null ? "M" : size.name());
    }

    /** Bước: mức đường (%). */
    DrinkBuilder sugar(int percent);

    /** Bước: mức đá (%). */
    DrinkBuilder ice(int percent);

    /** Bước: thêm một phần topping. */
    DrinkBuilder addTopping(Topping topping);

    /** Bước: số lượng ly. */
    DrinkBuilder quantity(int quantity);

    /** Bước: ghi chú cho quầy pha chế. */
    DrinkBuilder note(String note);

    /** getResult(): kiểm tra hợp lệ rồi trả về sản phẩm hoàn chỉnh. */
    OrderItem build();
}
