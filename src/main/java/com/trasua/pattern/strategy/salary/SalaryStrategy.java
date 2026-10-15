package com.trasua.pattern.strategy.salary;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: STRATEGY (interface chung của họ thuật toán tính lương)
 * ============================================================================
 * Mỗi loại nhân viên có một cách tính lương khác nhau:
 *   - PartTimeHourlyStrategy  : bán thời gian, trả theo giờ làm
 *   - FullTimeMonthlyStrategy : toàn thời gian, lương cứng + giờ làm thêm
 * PayrollCalculator (Context) chỉ biết interface này nên có thể ĐỔI CÁCH TÍNH lúc chạy
 * mà không phải sửa code tính lương ở chỗ khác.
 *
 * Interface này cũng là "Component" cho DECORATOR PATTERN (pattern/decorator/salary):
 * các lớp phụ cấp/thưởng bọc quanh một SalaryStrategy và cộng thêm tiền.
 */
public interface SalaryStrategy {

    /** Tính bảng lương của 1 nhân viên trong 1 tháng. */
    SalaryBreakdown calculate(SalaryInput input);

    /** Tên cách tính - hiển thị trên giao diện. */
    String getName();
}
