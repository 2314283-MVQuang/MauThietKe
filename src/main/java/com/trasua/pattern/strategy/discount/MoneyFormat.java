package com.trasua.pattern.strategy.discount;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** Định dạng tiền kiểu Việt Nam: 15000 -> "15.000đ". */
public final class MoneyFormat {

    private MoneyFormat() {
    }

    public static String vnd(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return format.format(amount == null ? BigDecimal.ZERO : amount) + "đ";
    }
}
