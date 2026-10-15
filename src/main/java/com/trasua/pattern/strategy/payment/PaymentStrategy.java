package com.trasua.pattern.strategy.payment;

import com.trasua.entity.PaymentMethod;

/**
 * ============================================================================
 *  STRATEGY PATTERN (họ #2: THANH TOÁN) - vai trò: STRATEGY (giao diện)
 * ============================================================================
 * Mỗi phương thức thanh toán có quy trình xử lý khác nhau:
 *   - Tiền mặt: kiểm tra tiền khách đưa, tính tiền thối.
 *   - Chuyển khoản Vietcombank: hiện mã VietQR, chờ nhân viên xác nhận đã nhận tiền.
 *   - Ví MoMo: hiện mã QR MoMo, chờ nhân viên xác nhận đã nhận tiền.
 * Ba quy trình được đóng gói thành 3 lớp cùng giao diện; OrderService chỉ gọi
 * {@code paymentProcessor.process(method, request)} mà không có if/else nào.
 */
public interface PaymentStrategy {

    /** Phương thức mà chiến lược này phụ trách (dùng làm khoá tra cứu). */
    PaymentMethod getMethod();

    /** Thực hiện thanh toán. Ném IllegalArgumentException nếu không hợp lệ. */
    PaymentResult pay(PaymentRequest request);

    /**
     * Đường dẫn ảnh mã QR để khách quét (null nếu phương thức không dùng QR).
     * Mỗi chiến lược tự cung cấp QR của mình -> giao diện không cần if/else
     * "nếu là ngân hàng thì hiện ảnh A, nếu là MoMo thì hiện ảnh B".
     */
    default String getQrImageUrl() {
        return null;
    }

    /**
     * Nội dung mã QR ĐỘNG kèm đúng số tiền + nội dung chuyển khoản (chuẩn VietQR).
     * null = phương thức này không dùng QR (VD tiền mặt) -> giao diện tự ẩn mã QR.
     */
    default String buildQrPayload(java.math.BigDecimal amount, String note) {
        return null;
    }

    /** Người nhận tiền hiển thị dưới mã QR (VD "Vietcombank · STK ... · MAI VAN QUANG"). */
    default String getQrReceiver() {
        return null;
    }
}
