package com.trasua.pattern.strategy.payment;

import com.trasua.entity.PaymentMethod;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: CONTEXT (cho họ thanh toán)
 * ============================================================================
 * Giữ một "bảng tra" PaymentMethod -> PaymentStrategy. Khi cần thanh toán,
 * chọn chiến lược theo phương thức khách chọn rồi ủy quyền xử lý.
 *
 * Danh sách chiến lược được Spring truyền vào (xem config/PatternConfig).
 * Thêm phương thức mới (ví dụ ZaloPay) = viết lớp ZaloPayPaymentStrategy và
 * đăng ký thêm - không sửa lớp này.
 */
public class PaymentProcessor {

    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    public PaymentProcessor(List<PaymentStrategy> strategyList) {
        for (PaymentStrategy strategy : strategyList) {
            strategies.put(strategy.getMethod(), strategy);
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Chưa hỗ trợ phương thức thanh toán: " + method);
        }
        return strategy;
    }

    /** Ảnh QR của từng phương thức (chỉ những phương thức có QR) - dùng cho giao diện. */
    public Map<String, String> qrImages() {
        Map<String, String> result = new java.util.LinkedHashMap<>();
        strategies.forEach((method, strategy) -> {
            if (strategy.getQrImageUrl() != null) {
                result.put(method.name(), strategy.getQrImageUrl());
            }
        });
        return result;
    }

    public PaymentResult process(PaymentMethod method, PaymentRequest request) {
        // ===> chọn chiến lược theo lựa chọn của khách rồi ỦY QUYỀN <===
        return getStrategy(method).pay(request);
    }
}
