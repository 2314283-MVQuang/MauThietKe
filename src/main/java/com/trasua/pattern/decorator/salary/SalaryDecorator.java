package com.trasua.pattern.decorator.salary;

import com.trasua.pattern.strategy.salary.SalaryBreakdown;
import com.trasua.pattern.strategy.salary.SalaryInput;
import com.trasua.pattern.strategy.salary.SalaryStrategy;

/**
 * ============================================================================
 *  DECORATOR PATTERN - vai trò: DECORATOR (lớp cha của các khoản phụ cấp / thưởng)
 * ============================================================================
 * Bọc một SalaryStrategy bất kỳ (lương theo giờ hoặc lương cứng) và CỘNG THÊM một khoản
 * mà không sửa lớp tính lương gốc - giống ToppingDecorator cộng giá topping vào ly nước.
 *
 *   new ThuongChuyenCanDecorator(new PhuCapCuoiTuanDecorator(new PartTimeHourlyStrategy()))
 *        -> lương theo giờ + phụ cấp cuối tuần + thưởng chuyên cần
 */
public abstract class SalaryDecorator implements SalaryStrategy {

    protected final SalaryStrategy wrapped;

    protected SalaryDecorator(SalaryStrategy wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Decorator phải bọc một cách tính lương");
        }
        this.wrapped = wrapped;
    }

    @Override
    public SalaryBreakdown calculate(SalaryInput input) {
        SalaryBreakdown base = wrapped.calculate(input);      // 1. để lớp bên trong tính trước
        return addition(input, base);                          // 2. cộng thêm phần của mình
    }

    @Override
    public String getName() {
        return wrapped.getName() + " + " + getAdditionName();
    }

    public SalaryStrategy getWrapped() {
        return wrapped;
    }

    /** Khoản cộng thêm (trả về base nếu không đủ điều kiện). */
    protected abstract SalaryBreakdown addition(SalaryInput input, SalaryBreakdown base);

    protected abstract String getAdditionName();
}
