package com.trasua.pattern;

import com.trasua.entity.DrinkSize;
import com.trasua.entity.OrderItem;
import com.trasua.entity.Product;
import com.trasua.entity.Topping;
import com.trasua.pattern.builder.OrderItemBuilder;
import com.trasua.pattern.decorator.BaseBeverage;
import com.trasua.pattern.decorator.Beverage;
import com.trasua.pattern.decorator.DecoratorChainTracer;
import com.trasua.pattern.decorator.ToppingDecorator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Nhiều phần cùng một topping: Builder thêm nhiều lần, Decorator bọc nhiều lớp. */
class ToppingPortionTest {

    private final Product traSua = BuilderPatternTest.product(1L, "Trà sữa truyền thống", 25000, 32000);
    private final Topping tranChau = BuilderPatternTest.topping("Trân châu đen", 5000);
    private final Topping kemCheese = BuilderPatternTest.topping("Kem cheese", 8000);

    @Test
    void haiPhanTranChauBocHaiLopVaMoTaGon() {
        Beverage ly = new BaseBeverage("Trà sữa", BigDecimal.valueOf(25000));
        ly = new ToppingDecorator(ly, "Trân châu đen", BigDecimal.valueOf(5000));
        ly = new ToppingDecorator(ly, "Trân châu đen", BigDecimal.valueOf(5000));
        assertEquals(0, BigDecimal.valueOf(35000).compareTo(ly.getCost()));
        assertEquals("Trà sữa + Trân châu đen x2", ly.getDescription());
        List<DecoratorChainTracer.Step> steps = DecoratorChainTracer.trace(ly);
        assertEquals(3, steps.size());                         // ly gốc + 2 lớp topping
    }

    @Test
    void builderThemNhieuPhanTopping() {
        OrderItem item = OrderItemBuilder.forProduct(traSua)
                .size(DrinkSize.M)
                .addTopping(tranChau, 2)
                .addTopping(kemCheese)
                .build();
        assertEquals(0, BigDecimal.valueOf(25000 + 2 * 5000 + 8000).compareTo(item.getUnitPrice()));
        assertEquals("Trân châu đen x2, Kem cheese", item.getToppings());
        assertTrue(item.getDescription().contains("Trân châu đen x2"));
    }

    @Test
    void toppingChonXenKeVanGopDung() {
        OrderItem item = OrderItemBuilder.forProduct(traSua)
                .toppings(List.of(tranChau, kemCheese, tranChau))   // khách bấm + trân châu 2 lần, xen kẽ
                .build();
        assertEquals("Trân châu đen x2, Kem cheese", item.getToppings());
        assertEquals(0, BigDecimal.valueOf(43000).compareTo(item.getUnitPrice()));
    }

    @Test
    void chanQuaSoPhanChoPhep() {
        assertThrows(IllegalStateException.class,
                () -> OrderItemBuilder.forProduct(traSua).addTopping(tranChau, 4).build());   // > 3 phần 1 loại
        assertThrows(IllegalStateException.class,
                () -> OrderItemBuilder.forProduct(traSua).addTopping(tranChau, 3).addTopping(kemCheese, 3).build()); // > 5 phần
    }
}
