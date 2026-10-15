package com.trasua.pattern.strategy.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kết quả tính lương: danh sách các khoản (lương chính, phụ cấp, thưởng...) và tổng tiền.
 * Mỗi Decorator sẽ trả về một SalaryBreakdown MỚI có thêm 1 dòng (giống Decorator đồ uống cộng thêm giá topping).
 */
public final class SalaryBreakdown {

    /** Một khoản tiền trong bảng lương. */
    public record Line(String label, BigDecimal amount) {
    }

    private final List<Line> lines;

    private SalaryBreakdown(List<Line> lines) {
        this.lines = Collections.unmodifiableList(lines);
    }

    public static SalaryBreakdown of(String label, BigDecimal amount) {
        return new SalaryBreakdown(List.of(new Line(label, round(amount))));
    }

    /** Trả về bảng lương mới = bảng lương cũ + 1 khoản. */
    public SalaryBreakdown plus(String label, BigDecimal amount) {
        List<Line> copy = new ArrayList<>(lines);
        copy.add(new Line(label, round(amount)));
        return new SalaryBreakdown(copy);
    }

    public List<Line> getLines() {
        return lines;
    }

    /** Tổng lương (VNĐ, đã làm tròn đến đồng). */
    public BigDecimal getTotal() {
        return lines.stream().map(Line::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal round(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(0, RoundingMode.HALF_UP);
    }
}
