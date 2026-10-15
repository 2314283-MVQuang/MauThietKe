package com.trasua.pattern.strategy.salary;

import java.math.BigDecimal;
import java.util.List;

/**
 * Đầu vào chung cho mọi chiến lược tính lương.
 *
 * @param shifts     các ca ĐÃ KẾT CA trong tháng
 * @param hourlyRate đơn giá/giờ hiện tại của nhân viên (dùng tính giờ làm thêm của nhân viên toàn thời gian)
 * @param baseSalary lương cứng/tháng (chỉ nhân viên toàn thời gian)
 */
public record SalaryInput(List<ShiftInfo> shifts, BigDecimal hourlyRate, BigDecimal baseSalary) {

    public SalaryInput {
        shifts = shifts == null ? List.of() : List.copyOf(shifts);
        hourlyRate = hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
        baseSalary = baseSalary == null ? BigDecimal.ZERO : baseSalary;
    }

    public long totalMinutes() {
        return shifts.stream().mapToLong(ShiftInfo::minutes).sum();
    }
}
