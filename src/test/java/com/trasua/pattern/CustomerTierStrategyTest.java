package com.trasua.pattern;

import com.trasua.pattern.strategy.tier.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử Strategy xếp hạng khách hàng: cùng 1 khách, đổi tiêu chí -> đổi hạng. */
class CustomerTierStrategyTest {

    private static CustomerStats stats(long orders, long spent) {
        return new CustomerStats(orders, BigDecimal.valueOf(spent));
    }

    @Test
    void theoChiTieu() {
        CustomerTierStrategy s = new SpendingTierStrategy();
        assertEquals(CustomerTier.DONG, s.classify(stats(1, 299_999)));
        assertEquals(CustomerTier.BAC, s.classify(stats(1, 300_000)));
        assertEquals(CustomerTier.VANG, s.classify(stats(1, 1_000_000)));
    }

    @Test
    void theoSoDon() {
        CustomerTierStrategy s = new OrderCountTierStrategy();
        assertEquals(CustomerTier.DONG, s.classify(stats(2, 5_000_000)));
        assertEquals(CustomerTier.BAC, s.classify(stats(3, 0)));
        assertEquals(CustomerTier.VANG, s.classify(stats(10, 0)));
    }

    @Test
    void context_doiTieuChiLucChay() {
        CustomerTierClassifier classifier = CustomerTierClassifier.withDefaults();
        CustomerStats khach = stats(12, 250_000);                 // nhiều đơn nhưng chi ít
        assertEquals("chi-tieu", classifier.getCurrent().getKey());
        assertEquals(CustomerTier.DONG, classifier.classify(khach));
        classifier.use("so-don");
        assertEquals(CustomerTier.VANG, classifier.classify(khach));
        classifier.use("khong-co");                               // khoá lạ -> giữ nguyên
        assertEquals("so-don", classifier.getCurrent().getKey());
        assertEquals(2, classifier.getAll().size());
    }
}
