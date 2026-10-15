package com.trasua.pattern.strategy.salary;

/**
 * ============================================================================
 *  STRATEGY PATTERN - vai trò: CONTEXT
 * ============================================================================
 * Giữ một tham chiếu tới SalaryStrategy và ủy quyền việc tính lương cho nó.
 * Gọi setStrategy(...) để đổi cách tính lúc chạy (giống PriceCalculator của mã giảm giá).
 */
public class PayrollCalculator {

    private SalaryStrategy strategy;

    public PayrollCalculator(SalaryStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(SalaryStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Chưa chọn cách tính lương");
        }
        this.strategy = strategy;
    }

    public SalaryStrategy getStrategy() {
        return strategy;
    }

    public SalaryBreakdown calculate(SalaryInput input) {
        return strategy.calculate(input);
    }
}
