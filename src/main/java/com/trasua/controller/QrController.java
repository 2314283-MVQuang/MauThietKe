package com.trasua.controller;

import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.PaymentStatus;
import com.trasua.pattern.strategy.payment.PaymentProcessor;
import com.trasua.service.OrderService;
import com.trasua.service.QrImageService;
import com.trasua.service.UserService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

/**
 * Ảnh mã QR THANH TOÁN của từng đơn: đúng số tiền của đơn, nội dung chuyển khoản = mã đơn,
 * tiền vào STK của quán (Vietcombank / MoMo - lấy từ application.properties).
 * STRATEGY: mỗi PaymentStrategy tự tạo nội dung QR của mình (buildQrPayload).
 */
@Controller
public class QrController {

    private final OrderService orderService;
    private final UserService userService;
    private final PaymentProcessor paymentProcessor;
    private final QrImageService qrImageService;

    public QrController(OrderService orderService, UserService userService,
                        PaymentProcessor paymentProcessor, QrImageService qrImageService) {
        this.orderService = orderService;
        this.userService = userService;
        this.paymentProcessor = paymentProcessor;
        this.qrImageService = qrImageService;
    }

    @GetMapping(value = "/qr/order/{id}.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> orderQr(@PathVariable("id") Long id, Principal principal) {
        // Khách chỉ lấy được QR đơn của mình (getForViewer kiểm tra quyền)
        Order order = orderService.getForViewer(id, userService.getByUsername(principal.getName()));
        if (order.getStatus() == OrderStatus.DA_HUY || order.getPaymentStatus() == PaymentStatus.DA_THANH_TOAN) {
            return ResponseEntity.notFound().build();
        }
        String payload = paymentProcessor.getStrategy(order.getPaymentMethod())
                .buildQrPayload(order.getTotal(), order.getOrderCode());
        if (payload == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImageService.png(payload, 480));
    }
}
