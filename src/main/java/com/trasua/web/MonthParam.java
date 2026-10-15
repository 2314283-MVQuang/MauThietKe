package com.trasua.web;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** Đọc tham số ?month=2026-10 (sai định dạng / bỏ trống -> tháng hiện tại). */
public final class MonthParam {

    private MonthParam() {
    }

    public static YearMonth parse(String value) {
        if (value == null || value.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }

    /** "10/2026" để hiển thị. */
    public static String label(YearMonth month) {
        return String.format("%02d/%d", month.getMonthValue(), month.getYear());
    }
}
