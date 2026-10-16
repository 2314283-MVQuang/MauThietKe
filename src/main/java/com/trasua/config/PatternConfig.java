package com.trasua.config;

import com.trasua.pattern.strategy.payment.BankTransferPaymentStrategy;
import com.trasua.pattern.strategy.payment.CashPaymentStrategy;
import com.trasua.pattern.strategy.payment.MomoPaymentStrategy;
import com.trasua.pattern.strategy.payment.PaymentProcessor;
import com.trasua.pattern.strategy.payment.PaymentStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Đăng ký các đối tượng của mẫu thiết kế làm Spring Bean.
 *
 * Các lớp trong package "pattern" được viết bằng Java thuần (không phụ thuộc
 * Spring) để dễ đọc, dễ kiểm thử và dễ trình bày. Lớp cấu hình này "nối" chúng
 * vào ứng dụng Spring Boot.
 */
@Configuration
public class PatternConfig {

    /** STRATEGY: mỗi phương thức thanh toán là một bean PaymentStrategy. */
    @Bean
    public CashPaymentStrategy cashPaymentStrategy() {
        return new CashPaymentStrategy();
    }

    @Bean
    public BankTransferPaymentStrategy bankTransferPaymentStrategy(
            @Value("${app.bank.name}") String bankName,
            @Value("${app.bank.account-number}") String accountNumber,
            @Value("${app.bank.account-name}") String accountName,
            @Value("${app.bank.qr-image}") String qrImage,
            @Value("${app.bank.bin:970436}") String bankBin) {
        return new BankTransferPaymentStrategy(bankName, accountNumber, accountName, qrImage, bankBin);
    }

    @Bean
    public MomoPaymentStrategy momoPaymentStrategy(
            @Value("${app.momo.account-name}") String accountName,
            @Value("${app.momo.qr-image}") String qrImage,
            @Value("${app.momo.qr-payload:}") String qrPayload) {
        return new MomoPaymentStrategy(accountName, qrImage, qrPayload);
    }

    /**
     * STRATEGY - Context: Spring tự gom TẤT CẢ bean kiểu PaymentStrategy vào
     * List và truyền vào đây. Thêm chiến lược mới chỉ cần khai báo thêm 1 @Bean.
     */
    @Bean
    public PaymentProcessor paymentProcessor(List<PaymentStrategy> strategies) {
        return new PaymentProcessor(strategies);
    }
}
