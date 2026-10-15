package com.trasua.pattern;

import com.trasua.entity.Category;
import com.trasua.entity.DrinkSize;
import com.trasua.entity.Order;
import com.trasua.entity.OrderItem;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.OrderType;
import com.trasua.entity.PaymentMethod;
import com.trasua.entity.Product;
import com.trasua.entity.Role;
import com.trasua.entity.Topping;
import com.trasua.entity.User;
import com.trasua.pattern.builder.DrinkDirector;
import com.trasua.pattern.builder.OrderBuilder;
import com.trasua.pattern.builder.OrderItemBuilder;
import com.trasua.pattern.strategy.discount.PercentageDiscountStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử Builder Pattern (OrderItemBuilder, OrderBuilder, Director). */
class BuilderPatternTest {

    private final Product traSua = product(1L, "Trà sữa truyền thống", 25000, 32000);
    private final Topping tranChau = topping("Trân châu đen", 5000);

    @Test
    void dungLyNuocTungBuocVaTinhGiaQuaDecorator() {
        OrderItem item = OrderItemBuilder.forProduct(traSua)
                .size(DrinkSize.L)
                .sugar(50)
                .ice(30)
                .addTopping(tranChau)
                .quantity(2)
                .build();

        assertEquals(0, BigDecimal.valueOf(37000).compareTo(item.getUnitPrice())); // 25k + 7k + 5k
        assertEquals(0, BigDecimal.valueOf(74000).compareTo(item.getLineTotal()));
        assertEquals("Trân châu đen", item.getToppings());
        assertTrue(item.getDescription().contains("(Size L)"));
    }

    @Test
    void giaTriMacDinhKhiKhongGoiBuocNao() {
        OrderItem item = OrderItemBuilder.forProduct(traSua).build();
        assertEquals("M", item.getSize());
        assertEquals(70, item.getSugarLevel());
        assertEquals(1, item.getQuantity());
    }

    @Test
    void builderChanDuLieuKhongHopLe() {
        assertThrows(IllegalStateException.class,
                () -> OrderItemBuilder.forProduct(traSua).sugar(45).build());
        assertThrows(IllegalStateException.class,
                () -> OrderItemBuilder.forProduct(traSua).quantity(0).build());
    }

    @Test
    void directorTaoLyTheoCongThuc() {
        DrinkDirector director = new DrinkDirector();
        OrderItem lessSweet = director.makeLessSweet(traSua);
        assertEquals(30, lessSweet.getSugarLevel());
        OrderItem special = director.makeSpecial(traSua, List.of(tranChau));
        assertEquals("L", special.getSize());
        assertEquals(0, BigDecimal.valueOf(37000).compareTo(special.getUnitPrice()));
    }

    @Test
    void dungDonHangGiaoHangCoKhuyenMai() {
        User khach = new User("khach", "x", "Khách A", Role.CUSTOMER);
        khach.setPhone("0901234567");
        khach.setAddress("Đà Lạt");

        OrderItem item = OrderItemBuilder.forProduct(traSua).quantity(4).build(); // 100.000đ
        Order order = OrderBuilder.newOrder()
                .forCustomer(khach)
                .orderType(OrderType.GIAO_HANG)
                .addItem(item)
                .discountStrategy(new PercentageDiscountStrategy(BigDecimal.TEN, BigDecimal.valueOf(20000), null), "GIAM10")
                .paymentMethod(PaymentMethod.VI_MOMO)
                .build();

        assertEquals(OrderStatus.CHO_XAC_NHAN, order.getStatus());
        assertEquals("Đà Lạt", order.getDeliveryAddress());
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(order.getSubtotal()));
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(order.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(90000).compareTo(order.getTotal()));
        assertEquals("GIAM10", order.getPromotionCode());
        assertTrue(order.getOrderCode().startsWith("TS"));
        assertSame(order, order.getItems().get(0).getOrder());
    }

    @Test
    void donGiaoHangThieuDiaChiBiTuChoi() {
        User staff = new User("nv", "x", "Nhân viên", Role.STAFF);
        OrderBuilder builder = OrderBuilder.newOrder()
                .createdByStaff(staff)
                .orderType(OrderType.GIAO_HANG)
                .addItem(OrderItemBuilder.forProduct(traSua).build());
        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().contains("địa chỉ"));
    }

    @Test
    void donTrongBiTuChoi() {
        User staff = new User("nv", "x", "Nhân viên", Role.STAFF);
        assertThrows(IllegalStateException.class,
                () -> OrderBuilder.newOrder().createdByStaff(staff).orderType(OrderType.TAI_QUAN).build());
    }

    @Test
    void donTaiQuayDuocXacNhanNgay() {
        User staff = new User("nv", "x", "Nhân viên", Role.STAFF);
        Order order = OrderBuilder.newOrder()
                .createdByStaff(staff)
                .orderType(OrderType.TAI_QUAN)
                .addItem(OrderItemBuilder.forProduct(traSua).build())
                .build();
        assertEquals(OrderStatus.DA_XAC_NHAN, order.getStatus());
        assertEquals("Khách lẻ", order.getCustomerName());
    }

    // ---------------------------------------------------------------------
    static Product product(Long id, String name, long m, long l) {
        Product p = new Product(name, "", new Category("Trà sữa", ""), BigDecimal.valueOf(m), BigDecimal.valueOf(l));
        p.setId(id);
        return p;
    }

    static Topping topping(String name, long price) {
        return new Topping(name, BigDecimal.valueOf(price));
    }
}
