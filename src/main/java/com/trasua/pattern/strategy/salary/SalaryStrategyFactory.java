package com.trasua.pattern.strategy.salary;

import com.trasua.entity.EmploymentType;
import com.trasua.pattern.decorator.salary.PhuCapCuoiTuanDecorator;
import com.trasua.pattern.decorator.salary.ThuongChuyenCanDecorator;

/**
 * Chọn chiến lược tính lương theo loại nhân viên rồi "khoác" thêm các Decorator phụ cấp/thưởng.
 * Đây là chỗ STRATEGY và DECORATOR gặp nhau:
 *   - Strategy  : chọn cách tính lương chính (theo giờ / lương cứng)
 *   - Decorator : cộng các khoản phụ cấp, thưởng lên trên cách tính đó
 */
public final class SalaryStrategyFactory {

    private SalaryStrategyFactory() {
    }

    public static SalaryStrategy forType(EmploymentType type) {
        SalaryStrategy strategy = (type == EmploymentType.FULL_TIME)
                ? new FullTimeMonthlyStrategy()
                : new PartTimeHourlyStrategy();
        // Bán thời gian được phụ cấp cuối tuần; mọi nhân viên đều có thể nhận thưởng chuyên cần
        if (type != EmploymentType.FULL_TIME) {
            strategy = new PhuCapCuoiTuanDecorator(strategy);
        }
        return new ThuongChuyenCanDecorator(strategy);
    }
}
