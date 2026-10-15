package com.trasua.pattern.decorator.salary;

import com.trasua.pattern.strategy.salary.SalaryBreakdown;
import com.trasua.pattern.strategy.salary.SalaryInput;
import com.trasua.pattern.strategy.salary.SalaryStrategy;

import java.math.BigDecimal;

/**
 * DECORATOR PATTERN - CONCRETE DECORATOR: thưởng chuyên cần.
 * Làm đủ từ 20 ca trong tháng được thưởng 200.000đ.
 */
public class ThuongChuyenCanDecorator extends SalaryDecorator {

    public static final int MIN_SHIFTS = 20;
    public static final BigDecimal BONUS = BigDecimal.valueOf(200_000);

    public ThuongChuyenCanDecorator(SalaryStrategy wrapped) {
        super(wrapped);
    }

    @Override
    protected SalaryBreakdown addition(SalaryInput input, SalaryBreakdown base) {
        if (input.shifts().size() < MIN_SHIFTS) {
            return base;
        }
        return base.plus("Thưởng chuyên cần (≥ " + MIN_SHIFTS + " ca)", BONUS);
    }

    @Override
    protected String getAdditionName() {
        return "thưởng chuyên cần";
    }
}
