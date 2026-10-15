package com.trasua.controller;

import com.trasua.entity.Order;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.pattern.strategy.payment.PaymentProcessor;
import com.trasua.service.OrderService;
import com.trasua.service.ReviewService;
import com.trasua.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Khách hàng xem lịch sử và chi tiết đơn; nhân viên/quản lý cũng dùng trang chi tiết. */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final PaymentProcessor paymentProcessor;
    private final ReviewService reviewService;

    public OrderController(OrderService orderService, UserService userService, PaymentProcessor paymentProcessor,
                           ReviewService reviewService) {
        this.reviewService = reviewService;
        this.paymentProcessor = paymentProcessor;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public String myOrders(Principal principal, Model model) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("orders", orderService.findByCustomer(user));
        return "order/my-orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Principal principal, Model model) {
        User user = userService.getByUsername(principal.getName());
        Order order = orderService.getForViewer(id, user);
        model.addAttribute("order", order);
        // STRATEGY: lấy QR từ chiến lược thanh toán của đơn (null với tiền mặt)
        var strategy = paymentProcessor.getStrategy(order.getPaymentMethod());
        model.addAttribute("paymentQr", strategy.getQrImageUrl());
        // QR ĐỘNG: đúng số tiền của đơn + nội dung = mã đơn (null nếu chưa cấu hình -> dùng ảnh QR tĩnh)
        boolean dynamicQr = strategy.buildQrPayload(order.getTotal(), order.getOrderCode()) != null;
        model.addAttribute("dynamicQr", dynamicQr);
        model.addAttribute("qrReceiver", strategy.getQrReceiver());
        // Đơn đã hoàn thành: khách được đánh giá từng món
        model.addAttribute("reviewedIds", user.getRole() == Role.CUSTOMER
                ? reviewService.reviewedProductIds(user) : java.util.Set.of());
        return "order/detail";
    }

    /** Hoá đơn in (khổ giấy máy in bill 80mm) - kèm mã QR đúng số tiền nếu đơn chưa thanh toán. */
    @GetMapping("/{id}/bill")
    public String bill(@PathVariable("id") Long id,
                       @org.springframework.web.bind.annotation.RequestParam(value = "print", required = false) String print,
                       Principal principal, Model model) {
        Order order = orderService.getForViewer(id, userService.getByUsername(principal.getName()));
        var strategy = paymentProcessor.getStrategy(order.getPaymentMethod());
        boolean needPay = order.getPaymentStatus() == com.trasua.entity.PaymentStatus.CHUA_THANH_TOAN
                && order.getStatus() != com.trasua.entity.OrderStatus.DA_HUY;
        model.addAttribute("order", order);
        model.addAttribute("showQr", needPay && strategy.buildQrPayload(order.getTotal(), order.getOrderCode()) != null);
        model.addAttribute("qrReceiver", strategy.getQrReceiver());
        model.addAttribute("autoPrint", print != null);
        return "order/bill";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, Principal principal, RedirectAttributes ra) {
        try {
            orderService.cancelByCustomer(id, userService.getByUsername(principal.getName()));
            ra.addFlashAttribute("success", "Đã huỷ đơn hàng");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
