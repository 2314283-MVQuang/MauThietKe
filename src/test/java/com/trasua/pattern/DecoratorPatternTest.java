package com.trasua.pattern;

import com.trasua.entity.DrinkSize;
import com.trasua.pattern.decorator.BaseBeverage;
import com.trasua.pattern.decorator.Beverage;
import com.trasua.pattern.decorator.DecoratorChainTracer;
import com.trasua.pattern.decorator.SizeDecorator;
import com.trasua.pattern.decorator.SugarIceDecorator;
import com.trasua.pattern.decorator.ToppingDecorator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Kiểm thử Decorator Pattern: bọc nhiều lớp, giá cộng dồn, mô tả nối dài. */
class DecoratorPatternTest {

    private static BigDecimal vnd(long v) {
        return BigDecimal.valueOf(v);
    }

    @Test
    void lyGocChuaBocGiDungGiaGoc() {
        Beverage traSua = new BaseBeverage("Trà sữa truyền thống", vnd(25000));
        assertEquals(0, vnd(25000).compareTo(traSua.getCost()));
        assertEquals("Trà sữa truyền thống", traSua.getDescription());
    }

    @Test
    void bocNhieuLopThiGiaCongDon() {
        Beverage ly = new BaseBeverage("Trà sữa truyền thống", vnd(25000));
        ly = new SizeDecorator(ly, DrinkSize.L, vnd(7000));          // +7.000
        ly = new ToppingDecorator(ly, "Trân châu đen", vnd(5000));    // +5.000
        ly = new ToppingDecorator(ly, "Kem cheese", vnd(10000));      // +10.000
        ly = new SugarIceDecorator(ly, 50, 70);                       // +0

        assertEquals(0, vnd(47000).compareTo(ly.getCost()));
        assertEquals("Trà sữa truyền thống (Size L) + Trân châu đen + Kem cheese [Đường 50%, Đá 70%]",
                ly.getDescription());
    }

    @Test
    void coTheBocCungMotToppingHaiLan() {
        Beverage ly = new BaseBeverage("Trà sữa", vnd(25000));
        ly = new ToppingDecorator(ly, "Trân châu đen", vnd(5000));
        ly = new ToppingDecorator(ly, "Trân châu đen", vnd(5000));
        assertEquals(0, vnd(35000).compareTo(ly.getCost()));
    }

    @Test
    void tracerBocTachDungTungLop() {
        Beverage ly = new BaseBeverage("Trà sữa", vnd(25000));
        ly = new SizeDecorator(ly, DrinkSize.L, vnd(7000));
        ly = new ToppingDecorator(ly, "Pudding trứng", vnd(8000));

        List<DecoratorChainTracer.Step> steps = DecoratorChainTracer.trace(ly);
        assertEquals(3, steps.size());
        assertEquals("Trà sữa", steps.get(0).label());
        assertEquals("Size L", steps.get(1).label());
        assertEquals(0, vnd(7000).compareTo(steps.get(1).added()));
        assertEquals("Pudding trứng", steps.get(2).label());
        assertEquals(0, vnd(40000).compareTo(steps.get(2).runningTotal()));
    }
}
