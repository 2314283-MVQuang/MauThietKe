package com.trasua.service;

import com.trasua.dto.CheckoutForm;
import com.trasua.entity.*;
import com.trasua.pattern.builder.OrderBuilder;
import com.trasua.pattern.strategy.discount.DiscountStrategy;
import com.trasua.pattern.strategy.discount.PriceCalculator;
import com.trasua.pattern.strategy.payment.PaymentProcessor;
import com.trasua.pattern.strategy.payment.PaymentRequest;
import com.trasua.pattern.strategy.payment.PaymentResult;
import com.trasua.repository.OrderRepository;
import com.trasua.web.Cart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Nghiệp vụ đơn hàng - nơi 3 mẫu thiết kế PHỐI HỢP với nhau:
 *
 *   Giỏ hàng (các OrderItem đã được tạo bởi OrderItemBuilder + chuỗi Decorator)
 *        │
 *        ▼
 *   OrderBuilder ──(dùng)──► DiscountStrategy (chọn theo mã khuyến mãi)
 *        │
 *        ▼
 *   Order hợp lệ ──► PaymentProcessor ──► PaymentStrategy (theo phương thức thanh toán)
 *        │
 *        ▼
 *   Lưu vào PostgreSQL
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PromotionService promotionService;
    private final PaymentProcessor paymentProcessor;
    private final com.trasua.repository.ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, PromotionService promotionService,
                        PaymentProcessor paymentProcessor, com.trasua.repository.ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.promotionService = promotionService;
        this.paymentProcessor = paymentProcessor;
        this.productRepository = productRepository;
    }

    /** Xem trước tiền của giỏ hàng (tạm tính, giảm giá, tổng) - dùng ở trang giỏ hàng. */
    public PriceCalculator.PriceBreakdown previewCart(Cart cart) {
        // STRATEGY: Context nhận chiến lược tương ứng với mã khuyến mãi khách nhập
        PriceCalculator calculator = new PriceCalculator(promotionService.strategyForCode(cart.getPromotionCode()));
        return calculator.calculate(cart.getItems());
    }

    /** Đặt hàng từ giỏ. */
    @Transactional
    public Order checkout(Cart cart, CheckoutForm form, User currentUser) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống");
        }
        // Món vừa được nhân viên đánh dấu HẾT sau khi khách bỏ vào giỏ -> không cho đặt
        for (OrderItem item : cart.getItems()) {
            if (item.getProductId() != null && productRepository.findById(item.getProductId())
                    .map(p -> !p.isAvailable()).orElse(false)) {
                throw new IllegalStateException("Món \"" + item.getProductName()
                        + "\" vừa hết - vui lòng xoá khỏi giỏ hoặc chọn món khác");
            }
        }

        // 1) STRATEGY: chọn chiến lược giảm giá theo mã khuyến mãi
        String promoCode = cart.getPromotionCode();
        DiscountStrategy discountStrategy = null;
        if (promoCode != null) {
            // Ném lỗi rõ ràng nếu mã vừa hết hạn trong lúc khách đang đặt
            promotionService.getUsableByCode(promoCode);
            discountStrategy = promotionService.strategyForCode(promoCode);
        }

        // 2) BUILDER: dựng đơn hàng từng bước
        OrderBuilder builder = OrderBuilder.newOrder()
                .orderType(form.getOrderType())
                .addItems(cart.getItems())
                .note(form.getNote())
                .discountStrategy(discountStrategy, promoCode)
                .paymentMethod(form.getPaymentMethod());

        boolean atCounter = currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN;
        if (atCounter) {
            builder.createdByStaff(currentUser)
                    .customerName(form.getCustomerName())
                    .phone(form.getPhone())
                    .deliveryAddress(form.getDeliveryAddress());
        } else {
            // Ưu tiên thông tin khách nhập ở form, thiếu thì lấy từ hồ sơ
            builder.customerName(form.getCustomerName())
                    .phone(form.getPhone())
                    .deliveryAddress(form.getDeliveryAddress())
                    .forCustomer(currentUser);
        }
        Order order = builder.build();   // <- kiểm tra hợp lệ + tính tiền tại đây

        // 3) STRATEGY: xử lý thanh toán theo phương thức đã chọn
        //    (khách online trả tiền mặt khi nhận hàng -> không truyền tiền khách đưa)
        PaymentRequest paymentRequest = new PaymentRequest(order.getOrderCode(), order.getTotal(),
                atCounter ? form.getCashReceived() : null);
        PaymentResult payment = paymentProcessor.process(order.getPaymentMethod(), paymentRequest);
        order.setPaymentStatus(payment.paid() ? PaymentStatus.DA_THANH_TOAN : PaymentStatus.CHUA_THANH_TOAN);
        order.setPaymentInfo(payment.info());
        if (payment.paid()) {
            order.setPaidAt(LocalDateTime.now());   // thu ngay tại quầy -> người thu = nhân viên tạo đơn
        }
        if (payment.paid() && order.getPaymentMethod() == PaymentMethod.TIEN_MAT) {
            order.setCashReceived(form.getCashReceived());
            order.setChangeAmount(payment.changeAmount());
        }

        Order saved = orderRepository.save(order);
        cart.clear();
        return saved;
    }

    // ============================ TRA CỨU ============================

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng #" + id));
    }

    /** Lấy đơn để xem, kiểm tra quyền: khách chỉ được xem đơn của mình. */
    public Order getForViewer(Long id, User viewer) {
        Order order = getById(id);
        if (viewer.getRole() == Role.CUSTOMER
                && (order.getCustomer() == null || !order.getCustomer().getId().equals(viewer.getId()))) {
            throw new NotFoundException("Không tìm thấy đơn hàng #" + id);
        }
        return order;
    }

    public List<Order> findByCustomer(User customer) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    public List<Order> findAll(OrderStatus status) {
        return status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    // ============================ XỬ LÝ ĐƠN ============================

    /** Nhân viên chuyển đơn sang bước tiếp theo. */
    @Transactional
    public Order moveToNextStatus(Long id, String actor) {
        Order order = getById(id);
        OrderStatus next = order.getStatus().next(order.getOrderType());
        if (next == null) {
            throw new IllegalStateException("Đơn đã kết thúc, không thể chuyển trạng thái");
        }
        if (next == OrderStatus.HOAN_THANH && order.getPaymentStatus() != PaymentStatus.DA_THANH_TOAN) {
            // Hoàn thành đơn tiền mặt = đã thu tiền khi giao / khi khách nhận
            if (order.getPaymentMethod() == PaymentMethod.TIEN_MAT) {
                markPaid(order, actor);
            } else {
                throw new IllegalStateException("Đơn chưa được xác nhận thanh toán, không thể hoàn thành");
            }
        }
        order.setStatus(next);
        order.setProcessedBy(actor);
        return order;
    }

    @Transactional
    public void cancelByStaff(Long id, String actor) {
        Order order = getById(id);
        if (order.getStatus().isFinished()) {
            throw new IllegalStateException("Đơn đã kết thúc, không thể huỷ");
        }
        order.setStatus(OrderStatus.DA_HUY);
        order.setCancelledBy(actor);
        order.setCancelledAt(LocalDateTime.now());
    }

    @Transactional
    public void cancelByCustomer(Long id, User customer) {
        Order order = getForViewer(id, customer);
        if (!order.isCancellableByCustomer()) {
            throw new IllegalStateException("Quán đã xác nhận đơn, bạn không thể tự huỷ. Vui lòng liên hệ quán.");
        }
        order.setStatus(OrderStatus.DA_HUY);
        order.setCancelledBy(customer.getUsername());
        order.setCancelledAt(LocalDateTime.now());
    }

    @Transactional
    public void confirmPayment(Long id, String actor) {
        Order order = getById(id);
        if (order.getStatus() == OrderStatus.DA_HUY) {
            throw new IllegalStateException("Đơn đã huỷ");
        }
        if (order.getPaymentStatus() == PaymentStatus.DA_THANH_TOAN) {
            throw new IllegalStateException("Đơn đã được thanh toán trước đó");
        }
        markPaid(order, actor);
    }

    /** Ghi nhận đã thu tiền + ai thu + lúc nào. */
    private static void markPaid(Order order, String actor) {
        order.setPaymentStatus(PaymentStatus.DA_THANH_TOAN);
        order.setPaidConfirmedBy(actor);
        order.setPaidAt(LocalDateTime.now());
    }

    /** Đơn tạo trong 1 ngày (lọc thêm theo trạng thái nếu có), mới nhất trước. */
    public List<Order> findByDate(java.time.LocalDate date, OrderStatus status) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        List<Order> orders = orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(from, to);
        return status == null ? orders : orders.stream().filter(o -> o.getStatus() == status).toList();
    }

    /** Số đơn theo từng ngày trong tuần chứa ngày {@code date} (cho thanh chọn ngày). */
    public java.util.Map<java.time.LocalDate, Long> countByDayOfWeek(java.time.LocalDate date) {
        java.time.LocalDate monday = com.trasua.web.DayPicker.weekStart(date);
        return orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                        monday.atStartOfDay(), monday.plusDays(7).atStartOfDay()).stream()
                .collect(java.util.stream.Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate(),
                        java.util.stream.Collectors.counting()));
    }

    /** Số đơn CHƯA KẾT THÚC còn treo từ những ngày trước ngày đang xem. */
    public long countUnfinishedBefore(java.time.LocalDate date) {
        return orderRepository.countByStatusInAndCreatedAtLessThan(
                List.of(OrderStatus.CHO_XAC_NHAN, OrderStatus.DA_XAC_NHAN, OrderStatus.DANG_PHA_CHE, OrderStatus.DANG_GIAO),
                date.atStartOfDay());
    }

    /** Ngày gần nhất (trước ngày đang xem) còn đơn chưa kết thúc. */
    public java.time.LocalDate oldestUnfinishedDate(java.time.LocalDate before) {
        return orderRepository.findFirstByStatusInAndCreatedAtLessThanOrderByCreatedAtAsc(
                        List.of(OrderStatus.CHO_XAC_NHAN, OrderStatus.DA_XAC_NHAN, OrderStatus.DANG_PHA_CHE, OrderStatus.DANG_GIAO),
                        before.atStartOfDay())
                .map(o -> o.getCreatedAt().toLocalDate()).orElse(null);
    }
}
