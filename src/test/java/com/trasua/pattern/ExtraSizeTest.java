package com.trasua.pattern;

import com.trasua.entity.OrderItem;
import com.trasua.entity.Product;
import com.trasua.entity.ProductSize;
import com.trasua.pattern.builder.OrderItemBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Size quản lý tự thêm (S, XL): Builder chọn size theo mã, SizeDecorator cộng/trừ chênh lệch so với M. */
class ExtraSizeTest {

    private Product traSua() {
        Product p = BuilderPatternTest.product(1L, "Trà sữa truyền thống", 25000, 32000);
        p.getExtraSizes().add(new ProductSize(p, "S", BigDecimal.valueOf(20000)));
        p.getExtraSizes().add(new ProductSize(p, "XL", BigDecimal.valueOf(39000)));
        return p;
    }

    @Test
    void sizeOptionsXepTheoGia() {
        assertEquals(java.util.List.of("S", "M", "L", "XL"),
                traSua().getSizeOptions().stream().map(Product.SizeOption::code).toList());
    }

    @Test
    void sizeXlCongThemChenhLech() {
        OrderItem item = OrderItemBuilder.forProduct(traSua()).size("xl").build();
        assertEquals(0, BigDecimal.valueOf(39000).compareTo(item.getUnitPrice()));
        assertEquals("XL", item.getSize());
        assertTrue(item.getDescription().contains("(Size XL)"));
    }

    @Test
    void sizeSReHonSizeM() {
        OrderItem item = OrderItemBuilder.forProduct(traSua()).size("S").addTopping(BuilderPatternTest.topping("Trân châu", 5000)).build();
        assertEquals(0, BigDecimal.valueOf(25000).compareTo(item.getUnitPrice()));   // 20.000 + 5.000
    }

    @Test
    void sizeKhongCoThiBaoLoi() {
        assertThrows(IllegalStateException.class, () -> OrderItemBuilder.forProduct(traSua()).size("XXL").build());
    }
}
