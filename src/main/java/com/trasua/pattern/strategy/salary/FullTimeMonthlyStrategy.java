package com.trasua.pattern.strategy.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: CONCRETE STRATEGY (nhân viên TOÀN THỜI GIAN)
 * ============================================================================
 * Lương = lương cứng + giờ làm thêm x đơn giá/giờ x 1,5.
 * Giờ làm thêm = phần vượt quá 208 giờ/tháng (26 ngày x 8 giờ).
 */
public class FullTimeMonthlyStrategy implements SalaryStrategy {

    public static final long STANDARD_MINUTES = 208L * 60;
    public static final BigDecimal OVERTIME_RATE = new BigDecimal("1.5");

    @Override
    public SalaryBreakdown calculate(SalaryInput input) {
        SalaryBreakdown result = SalaryBreakdown.of("Lương cứng", input.baseSalary());
        long overtimeMinutes = Math.max(0, input.totalMinutes() - STANDARD_MINUTES);
        if (overtimeMinutes > 0) {
            BigDecimal overtime = input.hourlyRate().multiply(OVERTIME_RATE)
                    .multiply(BigDecimal.valueOf(overtimeMinutes))
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            result = result.plus("Làm thêm " + PayrollFormat.hours(overtimeMinutes) + " giờ (x1,5)", overtime);
        }
        return result;
    }

    @Override
    public String getName() {
        return "Toàn thời gian - lương cứng + làm thêm";
    }
}
