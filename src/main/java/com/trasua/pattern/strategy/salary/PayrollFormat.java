package com.trasua.pattern.strategy.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Tiện ích hiển thị số giờ: 255 phút -> "4,25". */
public final class PayrollFormat {

    private PayrollFormat() {
    }

    public static BigDecimal toHours(long minutes) {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public static String hours(long minutes) {
        return toHours(minutes).stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
