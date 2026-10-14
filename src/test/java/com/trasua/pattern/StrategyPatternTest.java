package com.trasua.pattern;

import com.trasua.entity.OrderItem;
import com.trasua.entity.PaymentMethod;
import com.trasua.entity.Promotion;
import com.trasua.entity.PromotionType;
import com.trasua.pattern.strategy.discount.*;
import com.trasua.pattern.strategy.payment.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử Strategy Pattern: cùng một Context, đổi chiến lược -> đổi kết quả. */
class StrategyPatternTest {

    private static BigDecimal vnd(long v) {
        return BigDecimal.valueOf(v);
    }

    /** Giỏ: 2 ly 30.000 + 1 ly 25.000 = 85.000đ (3 ly). */
    private List<OrderItem> cart() {
        return List.of(item(30000, 2), item(25000, 1));
    }

    private static OrderItem item(long unit, int qty) {
        OrderItem i = new OrderItem();
        i.setUnitPrice(vnd(unit));
        i.setQuantity(qty);
        i.setLineTotal(vnd(unit * qty));
        return i;
    }

    @Test
    void cungContextDoiChienLuocLucChay() {
        PriceCalculator calculator = new PriceCalculator();   // mặc định: không giảm
        assertEquals(0, vnd(85000).compareTo(calculator.calculate(cart()).total()));

        calculator.setDiscountStrategy(new PercentageDiscountStrategy(BigDecimal.TEN, null, null));
        assertEquals(0, vnd(76500).compareTo(calculator.calculate(cart()).total()));      // -8.500

        calculator.setDiscountStrategy(new FixedAmountDiscountStrategy(vnd(15000), vnd(80000)));
        assertEquals(0, vnd(70000).compareTo(calculator.calculate(cart()).total()));      // -15.000

        calculator.setDiscountStrategy(new BuyXGetOneFreeStrategy(2));
        assertEquals(0, vnd(60000).compareTo(calculator.calculate(cart()).total()));      // tặng ly 25.000
    }

    @Test
    void giamPhanTramBiGioiHanToiDa() {
        DiscountResult r = new PercentageDiscountStrategy(vnd(50), vnd(20000), null)
                .calculate(DiscountInput.fromItems(cart()));
        assertEquals(0, vnd(20000).compareTo(r.amount()));
    }

    @Test
    void chuaDatDonToiThieuThiKhongGiam() {
        DiscountResult r = new FixedAmountDiscountStrategy(vnd(15000), vnd(100000))
                .calculate(DiscountInput.fromItems(cart()));
        assertFalse(r.applied());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.amount()));
    }

    @Test
    void muaXTang1CanDuSoLy() {
        DiscountResult r = new BuyXGetOneFreeStrategy(3).calculate(DiscountInput.fromItems(cart()));
        assertFalse(r.applied()); // 3 ly < 4 ly
    }

    @Test
    void factoryChonDungChienLuocTheoLoaiKhuyenMai() {
        Promotion p = new Promotion();
        p.setType(PromotionType.MUA_X_TANG_1);
        p.setBuyQuantity(2);
        assertInstanceOf(BuyXGetOneFreeStrategy.class, DiscountStrategyFactory.fromPromotion(p));

        p.setType(PromotionType.SO_TIEN);
        p.setValue(vnd(10000));
        assertInstanceOf(FixedAmountDiscountStrategy.class, DiscountStrategyFactory.fromPromotion(p));

        assertInstanceOf(NoDiscountStrategy.class, DiscountStrategyFactory.fromPromotion(null));
    }

    @Test
    void thanhToanTienMatTinhTienThoi() {
        PaymentProcessor processor = new PaymentProcessor(List.of(
                new CashPaymentStrategy(),
                new MomoPaymentStrategy("MAI VAN QUANG", "/images/payment/momo-qr.png"),
                new BankTransferPaymentStrategy("Vietcombank", "1035407425", "MAI VAN QUANG", "/images/payment/vietcombank-qr.png")));

        PaymentResult cash = processor.process(PaymentMethod.TIEN_MAT, new PaymentRequest("TS1", vnd(64000), vnd(100000)));
        assertTrue(cash.paid());
        assertEquals(0, vnd(36000).compareTo(cash.changeAmount()));

        assertThrows(IllegalArgumentException.class,
                () -> processor.process(PaymentMethod.TIEN_MAT, new PaymentRequest("TS1", vnd(64000), vnd(50000))));

        PaymentResult bank = processor.process(PaymentMethod.CHUYEN_KHOAN, new PaymentRequest("TS1", vnd(64000), null));
        assertFalse(bank.paid());
        assertTrue(bank.info().contains("TS1"));

        PaymentResult momo = processor.process(PaymentMethod.VI_MOMO, new PaymentRequest("TS1", vnd(64000), null));
        assertFalse(momo.paid());              // chờ nhân viên xác nhận đã nhận tiền
        assertTrue(momo.info().contains("TS1"));

        // Mỗi chiến lược tự cung cấp QR: tiền mặt không có, ngân hàng & MoMo có
        assertEquals(null, processor.getStrategy(PaymentMethod.TIEN_MAT).getQrImageUrl());
        assertEquals("/images/payment/momo-qr.png", processor.qrImages().get("VI_MOMO"));
        assertEquals("/images/payment/vietcombank-qr.png", processor.qrImages().get("CHUYEN_KHOAN"));
    }
}
