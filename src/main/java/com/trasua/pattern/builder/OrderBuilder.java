package com.trasua.pattern.builder;

import com.trasua.entity.Order;
import com.trasua.entity.OrderItem;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.OrderType;
import com.trasua.entity.PaymentMethod;
import com.trasua.entity.PaymentStatus;
import com.trasua.entity.User;
import com.trasua.pattern.strategy.discount.DiscountStrategy;
import com.trasua.pattern.strategy.discount.NoDiscountStrategy;
import com.trasua.pattern.strategy.discount.PriceCalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ============================================================================
 *  BUILDER PATTERN - CONCRETE BUILDER #2: dựng một ĐƠN HÀNG (Order)
 * ============================================================================
 * Order là đối tượng PHỨC TẠP: ~20 thuộc tính, có ràng buộc giữa chúng:
 *   - phải có ít nhất 1 món;
 *   - đơn GIAO_HANG bắt buộc có địa chỉ + số điện thoại;
 *   - đơn do nhân viên tạo tại quầy thì được xác nhận ngay, đơn online thì
 *     "chờ xác nhận";
 *   - tổng tiền = tạm tính - giảm giá (giảm giá do Strategy tính);
 *   - mã đơn phải được sinh tự động...
 *
 * Builder tách QUÁ TRÌNH XÂY DỰNG đối tượng ra khỏi đối tượng:
 *   - Client gọi từng bước theo nhu cầu (khách online / nhân viên tại quầy
 *     dùng các bước khác nhau nhưng cùng một Builder).
 *   - build() kiểm tra mọi ràng buộc + tính toán các trường dẫn xuất, nên
 *     KHÔNG THỂ tạo ra một Order "nửa vời" / sai dữ liệu.
 *
 * Ví dụ (trong OrderService):
 *     Order order = OrderBuilder.newOrder()
 *             .forCustomer(khach)
 *             .orderType(OrderType.GIAO_HANG)
 *             .deliveryAddress("01 Phù Đổng Thiên Vương, Đà Lạt")
 *             .addItems(gioHang)
 *             .discountStrategy(strategy, "GIAM10")   // <- STRATEGY
 *             .paymentMethod(PaymentMethod.VI_MOMO)
 *             .build();
 */
public class OrderBuilder {

    private static final DateTimeFormatter CODE_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private User customer;
    private User staff;
    private String customerName;
    private String phone;
    private OrderType orderType = OrderType.MANG_DI;
    private String deliveryAddress;
    private String note;
    private final List<OrderItem> items = new ArrayList<>();
    private DiscountStrategy discountStrategy = new NoDiscountStrategy();
    private String promotionCode;
    private PaymentMethod paymentMethod = PaymentMethod.TIEN_MAT;

    private OrderBuilder() {
    }

    public static OrderBuilder newOrder() {
        return new OrderBuilder();
    }

    // ------------------------- CÁC BƯỚC DỰNG -------------------------

    /** Đơn của khách có tài khoản: tự lấy sẵn tên, SĐT, địa chỉ từ hồ sơ. */
    public OrderBuilder forCustomer(User customer) {
        this.customer = customer;
        if (customer != null) {
            if (customerName == null) customerName = customer.getFullName();
            if (phone == null) phone = customer.getPhone();
            if (deliveryAddress == null) deliveryAddress = customer.getAddress();
        }
        return this;
    }

    /** Đơn do nhân viên tạo tại quầy. */
    public OrderBuilder createdByStaff(User staff) {
        this.staff = staff;
        return this;
    }

    public OrderBuilder customerName(String customerName) {
        if (customerName != null && !customerName.isBlank()) {
            this.customerName = customerName.trim();
        }
        return this;
    }

    public OrderBuilder phone(String phone) {
        if (phone != null && !phone.isBlank()) {
            this.phone = phone.trim();
        }
        return this;
    }

    public OrderBuilder orderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public OrderBuilder deliveryAddress(String address) {
        if (address != null && !address.isBlank()) {
            this.deliveryAddress = address.trim();
        }
        return this;
    }

    public OrderBuilder note(String note) {
        this.note = (note == null || note.isBlank()) ? null : note.trim();
        return this;
    }

    public OrderBuilder addItem(OrderItem item) {
        if (item != null) {
            items.add(item);
        }
        return this;
    }

    public OrderBuilder addItems(Collection<OrderItem> items) {
        if (items != null) {
            items.forEach(this::addItem);
        }
        return this;
    }

    /** Gắn chiến lược giảm giá (STRATEGY) sẽ dùng khi tính tổng tiền. */
    public OrderBuilder discountStrategy(DiscountStrategy strategy, String promotionCode) {
        this.discountStrategy = strategy == null ? new NoDiscountStrategy() : strategy;
        this.promotionCode = (strategy == null) ? null : promotionCode;
        return this;
    }

    public OrderBuilder paymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    // ------------------------------ BUILD ------------------------------

    public Order build() {
        validate();

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomer(customer);
        order.setStaff(staff);
        order.setCustomerName(customerName == null ? "Khách lẻ" : customerName);
        order.setPhone(phone);
        order.setOrderType(orderType);
        order.setDeliveryAddress(orderType == OrderType.GIAO_HANG ? deliveryAddress : null);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(PaymentStatus.CHUA_THANH_TOAN);
        // Đơn nhân viên tạo tại quầy đã được xác nhận ngay; đơn online chờ quán xác nhận
        order.setStatus(staff != null ? OrderStatus.DA_XAC_NHAN : OrderStatus.CHO_XAC_NHAN);

        // Sao chép từng món sang đơn (không dùng lại object trong giỏ hàng)
        for (OrderItem source : items) {
            order.addItem(copyOf(source));
        }

        // Tính tiền: Context của STRATEGY ủy quyền cho chiến lược giảm giá đã chọn
        PriceCalculator calculator = new PriceCalculator(discountStrategy);
        PriceCalculator.PriceBreakdown price = calculator.calculate(order.getItems());
        order.setSubtotal(price.subtotal());
        order.setDiscountAmount(price.discount().amount());
        order.setDiscountDescription(price.discount().applied() ? price.discount().message() : null);
        order.setPromotionCode(price.discount().applied() ? promotionCode : null);
        order.setTotal(price.total());
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private void validate() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Đơn hàng phải có ít nhất 1 món");
        }
        if (orderType == null) {
            throw new IllegalStateException("Chưa chọn hình thức nhận hàng");
        }
        if (paymentMethod == null) {
            throw new IllegalStateException("Chưa chọn phương thức thanh toán");
        }
        if (customer == null && staff == null) {
            throw new IllegalStateException("Đơn phải gắn với khách hàng hoặc nhân viên tạo đơn");
        }
        if (orderType == OrderType.GIAO_HANG) {
            if (deliveryAddress == null || deliveryAddress.isBlank()) {
                throw new IllegalStateException("Đơn giao hàng phải có địa chỉ giao");
            }
            if (phone == null || !phone.matches("0\\d{9,10}")) {
                throw new IllegalStateException("Đơn giao hàng phải có số điện thoại hợp lệ (10-11 số, bắt đầu bằng 0)");
            }
        }
        if (orderType == OrderType.TAI_QUAN && staff == null) {
            throw new IllegalStateException("Đơn uống tại quán chỉ được tạo bởi nhân viên tại quầy");
        }
    }

    private static String generateOrderCode() {
        return "TS" + LocalDateTime.now().format(CODE_FORMAT)
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private static OrderItem copyOf(OrderItem source) {
        OrderItem copy = new OrderItem();
        copy.setProductId(source.getProductId());
        copy.setProductName(source.getProductName());
        copy.setSize(source.getSize());
        copy.setSugarLevel(source.getSugarLevel());
        copy.setIceLevel(source.getIceLevel());
        copy.setToppings(source.getToppings());
        copy.setDescription(source.getDescription());
        copy.setQuantity(source.getQuantity());
        copy.setUnitPrice(source.getUnitPrice());
        copy.setLineTotal(source.getLineTotal());
        copy.setNote(source.getNote());
        return copy;
    }
}
