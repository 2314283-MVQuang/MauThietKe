package com.trasua.pattern.strategy.salary;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: CONCRETE STRATEGY (nhân viên BÁN THỜI GIAN)
 * ============================================================================
 * Lương = tổng (số giờ của từng ca x đơn giá/giờ của ca đó).
 * Dùng đơn giá chụp lại trong từng ca nên tăng lương giữa tháng vẫn tính đúng.
 * Ví dụ: 3 ca x 4 giờ x 25.000đ = 300.000đ.
 */
public class PartTimeHourlyStrategy implements SalaryStrategy {

    @Override
    public SalaryBreakdown calculate(SalaryInput input) {
        BigDecimal total = input.shifts().stream()
                .map(ShiftInfo::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return SalaryBreakdown.of("Lương theo giờ (" + PayrollFormat.hours(input.totalMinutes()) + " giờ)", total);
    }

    @Override
    public String getName() {
        return "Bán thời gian - trả theo giờ";
    }
}
