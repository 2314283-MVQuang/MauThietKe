package com.trasua.service;

import com.trasua.entity.PayrollRecord;
import com.trasua.entity.User;
import com.trasua.entity.WorkShift;
import com.trasua.pattern.strategy.salary.PayrollFormat;
import com.trasua.pattern.strategy.salary.SalaryBreakdown;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/** Bảng lương 1 nhân viên trong 1 tháng (kết quả của PayrollService). */
public class PayrollSummary {

    private final User staff;
    private final YearMonth month;
    private final List<WorkShift> shifts;
    private final int shiftCount;
    private final long totalMinutes;
    private final SalaryBreakdown breakdown;
    private final String strategyName;
    private final WorkShift openShift;
    private final PayrollRecord paidRecord;

    public PayrollSummary(User staff, YearMonth month, List<WorkShift> shifts, int shiftCount, long totalMinutes,
                          SalaryBreakdown breakdown, String strategyName, WorkShift openShift, PayrollRecord paidRecord) {
        this.staff = staff;
        this.month = month;
        this.shifts = shifts;
        this.shiftCount = shiftCount;
        this.totalMinutes = totalMinutes;
        this.breakdown = breakdown;
        this.strategyName = strategyName;
        this.openShift = openShift;
        this.paidRecord = paidRecord;
    }

    public User getStaff() { return staff; }
    public YearMonth getMonth() { return month; }
    /** Tất cả ca trong tháng (kể cả ca đang mở). */
    public List<WorkShift> getShifts() { return shifts; }
    /** Số ca đã kết thúc (được tính lương). */
    public int getShiftCount() { return shiftCount; }
    public long getTotalMinutes() { return totalMinutes; }
    public BigDecimal getTotalHours() { return PayrollFormat.toHours(totalMinutes); }
    public String getHoursText() { return PayrollFormat.hours(totalMinutes); }
    public SalaryBreakdown getBreakdown() { return breakdown; }
    public String getStrategyName() { return strategyName; }
    /** Ca đang mở (chưa kết ca) - không được tính lương cho đến khi kết ca. */
    public WorkShift getOpenShift() { return openShift; }
    public PayrollRecord getPaidRecord() { return paidRecord; }
    public boolean isPaid() { return paidRecord != null; }

    /** Lương tính theo dữ liệu hiện tại. */
    public BigDecimal getAmount() { return breakdown.getTotal(); }

    /** Số tiền phải trả: đã chốt thì lấy số đã chốt, chưa chốt thì lấy số tính hiện tại. */
    public BigDecimal getPayable() { return isPaid() ? paidRecord.getAmount() : getAmount(); }

    /** Đã chốt nhưng dữ liệu ca sau đó bị sửa -> số tính lại khác số đã trả. */
    public boolean isChangedAfterPaid() {
        return isPaid() && paidRecord.getAmount().compareTo(getAmount()) != 0;
    }

    public long getWarningCount() {
        return shifts.stream().filter(WorkShift::isWarning).count();
    }
}
