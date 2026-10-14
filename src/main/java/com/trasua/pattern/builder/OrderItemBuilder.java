package com.trasua.pattern.builder;

import com.trasua.entity.DrinkSize;
import com.trasua.entity.OrderItem;
import com.trasua.entity.Product;
import com.trasua.entity.Topping;
import com.trasua.pattern.decorator.BaseBeverage;
import com.trasua.pattern.decorator.Beverage;
import com.trasua.pattern.decorator.SizeDecorator;
import com.trasua.pattern.decorator.SugarIceDecorator;
import com.trasua.pattern.decorator.ToppingDecorator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 *  BUILDER PATTERN - CONCRETE BUILDER #1: dựng một ly nước (OrderItem)
 * ============================================================================
 * Một ly nước có nhiều tuỳ chọn: size, mức đường, mức đá, nhiều topping,
 * số lượng, ghi chú. Phần lớn là TUỲ CHỌN và có giá trị mặc định.
 *
 * Nếu dùng constructor ta sẽ có kiểu:
 *     new OrderItem(product, L, 50, 70, toppings, 2, "ít ngọt")
 * -> khó đọc (50 là đường hay đá?), dễ truyền nhầm thứ tự tham số
 *    ("telescoping constructor" anti-pattern).
 *
 * Với Builder:
 *     OrderItemBuilder.forProduct(traSua)
 *         .size(DrinkSize.L)
 *         .sugar(50)
 *         .ice(70)
 *         .addTopping(tranChauDen)
 *         .quantity(2)
 *         .build();
 * -> đọc như câu văn, chỉ gọi bước nào cần, kiểm tra hợp lệ tập trung ở build().
 *
 * KẾT HỢP VỚI DECORATOR: trong build(), builder ráp chuỗi Decorator
 *   BaseBeverage -> SizeDecorator -> ToppingDecorator... -> SugarIceDecorator
 * để tính đơn giá và mô tả ly nước.
 */
public class OrderItemBuilder {

    /** Các mức đường / đá cho phép (%). */
    public static final List<Integer> LEVELS = List.of(0, 30, 50, 70, 100);
    public static final int MAX_TOPPINGS = 5;
    public static final int MAX_QUANTITY = 50;

    // ---- các "phần" của sản phẩm đang được dựng (có giá trị mặc định) ----
    private final Product product;
    private DrinkSize size = DrinkSize.M;
    private int sugar = 70;
    private int ice = 70;
    private final List<Topping> toppings = new ArrayList<>();
    private int quantity = 1;
    private String note;

    private OrderItemBuilder(Product product) {
        this.product = product;
    }

    /** Điểm bắt đầu: bắt buộc phải có đồ uống. */
    public static OrderItemBuilder forProduct(Product product) {
        return new OrderItemBuilder(product);
    }

    // ------------------- CÁC BƯỚC DỰNG (trả về this để gọi nối) -------------------

    public OrderItemBuilder size(DrinkSize size) {
        this.size = size == null ? DrinkSize.M : size;
        return this;
    }

    public OrderItemBuilder sugar(int percent) {
        this.sugar = percent;
        return this;
    }

    public OrderItemBuilder ice(int percent) {
        this.ice = percent;
        return this;
    }

    public OrderItemBuilder addTopping(Topping topping) {
        if (topping != null) {
            this.toppings.add(topping);
        }
        return this;
    }

    public OrderItemBuilder toppings(Collection<Topping> toppings) {
        if (toppings != null) {
            toppings.forEach(this::addTopping);
        }
        return this;
    }

    public OrderItemBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderItemBuilder note(String note) {
        this.note = (note == null || note.isBlank()) ? null : note.trim();
        return this;
    }

    // ------------------------------ DỰNG KẾT QUẢ ------------------------------

    /**
     * Ráp chuỗi DECORATOR cho ly nước hiện tại.
     * Mỗi dòng "beverage = new XxxDecorator(beverage, ...)" là bọc thêm 1 lớp.
     */
    public Beverage buildBeverage() {
        validate();
        // Lõi: ly nước trơn với giá size M
        Beverage beverage = new BaseBeverage(product.getName(), product.getPriceM());

        // Lớp 1: size (L cộng thêm phần chênh lệch giá)
        BigDecimal sizeExtra = size == DrinkSize.L
                ? product.getPriceL().subtract(product.getPriceM())
                : BigDecimal.ZERO;
        beverage = new SizeDecorator(beverage, size, sizeExtra);

        // Lớp 2..n: mỗi topping là một lớp bọc
        for (Topping topping : toppings) {
            beverage = new ToppingDecorator(beverage, topping.getName(), topping.getPrice());
        }

        // Lớp cuối: ghi chú đường / đá (không đổi giá)
        beverage = new SugarIceDecorator(beverage, sugar, ice);
        return beverage;
    }

    /** Tạo OrderItem hoàn chỉnh, đã hợp lệ. */
    public OrderItem build() {
        Beverage beverage = buildBeverage(); // đã validate bên trong

        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setSize(size);
        item.setSugarLevel(sugar);
        item.setIceLevel(ice);
        item.setToppings(toppings.isEmpty() ? null
                : toppings.stream().map(Topping::getName).collect(Collectors.joining(", ")));
        item.setDescription(beverage.getDescription());
        item.setQuantity(quantity);
        item.setUnitPrice(beverage.getCost());
        item.setLineTotal(beverage.getCost().multiply(BigDecimal.valueOf(quantity)));
        item.setNote(note);
        return item;
    }

    /** Kiểm tra tính hợp lệ TẬP TRUNG tại một chỗ - ưu điểm lớn của Builder. */
    private void validate() {
        if (product == null) {
            throw new IllegalStateException("Chưa chọn đồ uống");
        }
        if (!product.isAvailable()) {
            throw new IllegalStateException("Món \"" + product.getName() + "\" hiện đang tạm hết");
        }
        if (!LEVELS.contains(sugar)) {
            throw new IllegalStateException("Mức đường không hợp lệ: " + sugar + "%");
        }
        if (!LEVELS.contains(ice)) {
            throw new IllegalStateException("Mức đá không hợp lệ: " + ice + "%");
        }
        if (toppings.size() > MAX_TOPPINGS) {
            throw new IllegalStateException("Mỗi ly chỉ được thêm tối đa " + MAX_TOPPINGS + " topping");
        }
        for (Topping topping : toppings) {
            if (!topping.isAvailable()) {
                throw new IllegalStateException("Topping \"" + topping.getName() + "\" hiện đang tạm hết");
            }
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalStateException("Số lượng phải từ 1 đến " + MAX_QUANTITY);
        }
    }
}
