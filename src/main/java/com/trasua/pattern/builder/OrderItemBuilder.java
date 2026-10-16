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
 *  BUILDER PATTERN - vai trò: CONCRETE BUILDER (cài đặt interface DrinkBuilder)
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
 * Vai trò trong mẫu:
 *   - implements DrinkBuilder  -> cài đặt chi tiết từng bước đã khai báo ở Builder;
 *   - giữ các "phần" của ly đang dựng (size, sugar, ice, toppings...);
 *   - build() (= getResult()) trả về sản phẩm hoàn chỉnh OrderItem.
 * Các hàm trả về OrderItemBuilder (kiểu con của DrinkBuilder - "covariant return")
 * nên vẫn gọi nối được cả những bước riêng như addTopping(t, 2), toppings(list).
 *
 * KẾT HỢP VỚI DECORATOR: trong build(), builder ráp chuỗi Decorator
 *   BaseBeverage -> SizeDecorator -> ToppingDecorator... -> SugarIceDecorator
 * để tính đơn giá và mô tả ly nước.
 */
public class OrderItemBuilder implements DrinkBuilder {

    /** Các mức đường / đá cho phép (%). */
    public static final List<Integer> LEVELS = List.of(0, 30, 50, 70, 100);
    public static final int MAX_TOPPINGS = 5;
    /** Mỗi loại topping tối đa bao nhiêu phần (VD 2 phần trân châu). */
    public static final int MAX_PER_TOPPING = 3;
    public static final int MAX_QUANTITY = 50;

    // ---- các "phần" của sản phẩm đang được dựng (có giá trị mặc định) ----
    private final Product product;
    /** Mã size: M, L hoặc size thêm của món (S, XL...). */
    private String size = "M";
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

    @Override
    public OrderItemBuilder size(DrinkSize size) {
        this.size = size == null ? "M" : size.name();
        return this;
    }

    /** Chọn size theo mã (kể cả size quản lý tự thêm, VD "XL"). */
    @Override
    public OrderItemBuilder size(String sizeCode) {
        this.size = sizeCode == null || sizeCode.isBlank() ? "M" : sizeCode.trim().toUpperCase();
        return this;
    }

    @Override
    public OrderItemBuilder sugar(int percent) {
        this.sugar = percent;
        return this;
    }

    @Override
    public OrderItemBuilder ice(int percent) {
        this.ice = percent;
        return this;
    }

    @Override
    public OrderItemBuilder addTopping(Topping topping) {
        if (topping != null) {
            this.toppings.add(topping);
        }
        return this;
    }

    /** Thêm nhiều phần của cùng một topping (VD 2 phần trân châu = bọc ToppingDecorator 2 lần). */
    public OrderItemBuilder addTopping(Topping topping, int portions) {
        for (int i = 0; i < portions; i++) {
            addTopping(topping);
        }
        return this;
    }

    public OrderItemBuilder toppings(Collection<Topping> toppings) {
        if (toppings != null) {
            toppings.forEach(this::addTopping);
        }
        return this;
    }

    @Override
    public OrderItemBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    @Override
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
        // Chênh lệch so với size M (size L / XL: cộng thêm; size S: âm -> rẻ hơn)
        BigDecimal sizeExtra = product.priceOfSize(size).orElseThrow().subtract(product.getPriceM());
        beverage = new SizeDecorator(beverage, "Size " + size, sizeExtra);

        // Lớp 2..n: mỗi PHẦN topping là một lớp bọc (2 phần trân châu = bọc 2 lần);
        // gom các phần cùng loại đứng liền nhau để mô tả gọn "Trân châu đen x2"
        for (Topping topping : groupedToppings()) {
            beverage = new ToppingDecorator(beverage, topping.getName(), topping.getPrice());
        }

        // Lớp cuối: ghi chú đường / đá (không đổi giá)
        beverage = new SugarIceDecorator(beverage, sugar, ice);
        return beverage;
    }

    /** getResult(): tạo OrderItem hoàn chỉnh, đã hợp lệ. */
    @Override
    public OrderItem build() {
        Beverage beverage = buildBeverage(); // đã validate bên trong

        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setSize(size);
        item.setSugarLevel(sugar);
        item.setIceLevel(ice);
        item.setToppings(toppings.isEmpty() ? null : toppingSummary());
        item.setDescription(beverage.getDescription());
        item.setQuantity(quantity);
        item.setUnitPrice(beverage.getCost());
        item.setLineTotal(beverage.getCost().multiply(BigDecimal.valueOf(quantity)));
        item.setNote(note);
        return item;
    }

    /** Danh sách topping, các phần cùng loại đứng liền nhau (giữ thứ tự chọn). */
    private List<Topping> groupedToppings() {
        java.util.Map<String, List<Topping>> groups = new java.util.LinkedHashMap<>();
        for (Topping t : toppings) {
            groups.computeIfAbsent(t.getName(), k -> new ArrayList<>()).add(t);
        }
        List<Topping> result = new ArrayList<>();
        groups.values().forEach(result::addAll);
        return result;
    }

    /** "Trân châu đen x2, Kem cheese" */
    private String toppingSummary() {
        java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
        for (Topping t : groupedToppings()) {
            counts.merge(t.getName(), 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .map(e -> e.getValue() > 1 ? e.getKey() + " x" + e.getValue() : e.getKey())
                .collect(Collectors.joining(", "));
    }

    /** Kiểm tra tính hợp lệ TẬP TRUNG tại một chỗ - ưu điểm lớn của Builder. */
    private void validate() {
        if (product == null) {
            throw new IllegalStateException("Chưa chọn đồ uống");
        }
        if (!product.isAvailable()) {
            throw new IllegalStateException("Món \"" + product.getName() + "\" hiện đang tạm hết");
        }
        if (product.priceOfSize(size).isEmpty()) {
            throw new IllegalStateException("Món \"" + product.getName() + "\" không có size " + size);
        }
        if (!LEVELS.contains(sugar)) {
            throw new IllegalStateException("Mức đường không hợp lệ: " + sugar + "%");
        }
        if (!LEVELS.contains(ice)) {
            throw new IllegalStateException("Mức đá không hợp lệ: " + ice + "%");
        }
        if (toppings.size() > MAX_TOPPINGS) {
            throw new IllegalStateException("Mỗi ly chỉ được thêm tối đa " + MAX_TOPPINGS + " phần topping");
        }
        java.util.Map<String, Long> perTopping = toppings.stream()
                .collect(Collectors.groupingBy(Topping::getName, Collectors.counting()));
        perTopping.forEach((name, n) -> {
            if (n > MAX_PER_TOPPING) {
                throw new IllegalStateException("Mỗi loại topping tối đa " + MAX_PER_TOPPING + " phần (" + name + ")");
            }
        });
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
