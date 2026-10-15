package com.trasua.entity;

import com.trasua.pattern.strategy.salary.PayrollFormat;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bảng lương ĐÃ CHỐT (đã trả) của 1 nhân viên trong 1 tháng.
 * Lưu lại số giờ và số tiền tại thời điểm trả để sau này sửa ca / đổi đơn giá
 * cũng không làm thay đổi lịch sử.
 */
@Entity
@Table(name = "payroll_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_payroll_staff_month", columnNames = {"staff_id", "payroll_month"}))
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    /** Tháng dạng "2026-10". */
    @Column(name = "payroll_month", nullable = false, length = 7)
    private String payrollMonth;

    @Column(name = "shift_count", nullable = false)
    private int shiftCount;

    @Column(name = "total_minutes", nullable = false)
    private long totalMinutes;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "paid_by", length = 50)
    private String paidBy;

    public String getHoursText() {
        return PayrollFormat.hours(totalMinutes);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }
    public String getPayrollMonth() { return payrollMonth; }
    public void setPayrollMonth(String payrollMonth) { this.payrollMonth = payrollMonth; }
    public int getShiftCount() { return shiftCount; }
    public void setShiftCount(int shiftCount) { this.shiftCount = shiftCount; }
    public long getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(long totalMinutes) { this.totalMinutes = totalMinutes; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getPaidBy() { return paidBy; }
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }
}
