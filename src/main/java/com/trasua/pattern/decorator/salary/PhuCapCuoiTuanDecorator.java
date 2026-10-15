package com.trasua.pattern.decorator.salary;

import com.trasua.pattern.strategy.salary.PayrollFormat;
import com.trasua.pattern.strategy.salary.SalaryBreakdown;
import com.trasua.pattern.strategy.salary.SalaryInput;
import com.trasua.pattern.strategy.salary.SalaryStrategy;
import com.trasua.pattern.strategy.salary.ShiftInfo;

import java.math.BigDecimal;

/**
 * DECORATOR PATTERN - CONCRETE DECORATOR: phụ cấp làm Thứ 7 / Chủ nhật.
 * Giờ làm cuối tuần được cộng thêm 20% đơn giá (tức là x1,2).
 */
public class PhuCapCuoiTuanDecorator extends SalaryDecorator {

    public static final BigDecimal PERCENT = new BigDecimal("0.2");

    public PhuCapCuoiTuanDecorator(SalaryStrategy wrapped) {
        super(wrapped);
    }

    @Override
    protected SalaryBreakdown addition(SalaryInput input, SalaryBreakdown base) {
        long minutes = 0;
        BigDecimal extra = BigDecimal.ZERO;
        for (ShiftInfo shift : input.shifts()) {
            if (shift.isWeekend()) {
                minutes += shift.minutes();
                extra = extra.add(shift.amount().multiply(PERCENT));
            }
        }
        if (minutes == 0) {
            return base;
        }
        return base.plus("Phụ cấp cuối tuần " + PayrollFormat.hours(minutes) + " giờ (+20%)", extra);
    }

    @Override
    protected String getAdditionName() {
        return "phụ cấp cuối tuần";
    }
}
