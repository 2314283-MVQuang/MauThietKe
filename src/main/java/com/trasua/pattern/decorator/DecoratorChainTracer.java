package com.trasua.pattern.decorator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tiện ích "bóc" từng lớp Decorator để hiển thị cách tính giá từng bước trên
 * giao diện (trang chọn món). Rất hữu ích khi demo cho thầy thấy ly nước được
 * bọc lần lượt như thế nào.
 *
 * Ví dụ kết quả:
 *   1. Trà sữa truyền thống        +25.000  = 25.000
 *   2. Size L                       + 7.000  = 32.000
 *   3. Trân châu đen                + 5.000  = 37.000
 *   4. Đường 50%, Đá 70%            +     0  = 37.000
 */
public final class DecoratorChainTracer {

    /** Một bước trong chuỗi: tên phần thêm, tiền cộng thêm, tổng tạm tính. */
    public record Step(String label, BigDecimal added, BigDecimal runningTotal) {
    }

    private DecoratorChainTracer() {
    }

    public static List<Step> trace(Beverage beverage) {
        // 1) Đi từ lớp ngoài cùng vào lõi
        List<Beverage> layers = new ArrayList<>();
        Beverage current = beverage;
        while (current instanceof BeverageDecorator decorator) {
            layers.add(decorator);
            current = decorator.getWrapped();
        }
        layers.add(current); // lõi: BaseBeverage
        Collections.reverse(layers); // đảo lại: từ lõi ra ngoài

        // 2) Tính phần tiền mà mỗi lớp cộng thêm
        List<Step> steps = new ArrayList<>();
        BigDecimal previous = BigDecimal.ZERO;
        for (Beverage layer : layers) {
            BigDecimal cost = layer.getCost();
            String label = (layer instanceof BeverageDecorator d) ? d.getAddition() : layer.getDescription();
            steps.add(new Step(label, cost.subtract(previous), cost));
            previous = cost;
        }
        return steps;
    }
}
