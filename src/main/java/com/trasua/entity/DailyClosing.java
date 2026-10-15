package com.trasua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Biên bản CHỐT KÉT cuối ngày: tiền mặt theo hoá đơn (hệ thống tính) so với tiền quản lý đếm thực tế.
 * Lệch âm = thiếu tiền (có thể nhân viên quỵt bill / thối nhầm), lệch dương = thừa.
 */
@Entity
@Table(name = "daily_closings",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_closing_date", columnNames = "business_date"))
public class DailyClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    /** Tiền mặt theo hoá đơn tại thời điểm chốt. */
    @Column(name = "expected_cash", nullable = false, precision = 14, scale = 2)
    private BigDecimal expectedCash = BigDecimal.ZERO;

    /** Tiền mặt đếm được trong két. */
    @Column(name = "counted_cash", nullable = false, precision = 14, scale = 2)
    private BigDecimal countedCash = BigDecimal.ZERO;

    /** Tiền chuyển khoản + MoMo theo hoá đơn (để đối chiếu sao kê). */
    @Column(name = "expected_transfer", precision = 14, scale = 2)
    private BigDecimal expectedTransfer = BigDecimal.ZERO;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "closed_by", length = 50)
    private String closedBy;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    /** Đếm được - theo hoá đơn. */
    public BigDecimal getDifference() {
        return countedCash.subtract(expectedCash);
    }

    public boolean isMatched() {
        return getDifference().signum() == 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }
    public BigDecimal getExpectedCash() { return expectedCash; }
    public void setExpectedCash(BigDecimal expectedCash) { this.expectedCash = expectedCash; }
    public BigDecimal getCountedCash() { return countedCash; }
    public void setCountedCash(BigDecimal countedCash) { this.countedCash = countedCash; }
    public BigDecimal getExpectedTransfer() { return expectedTransfer; }
    public void setExpectedTransfer(BigDecimal expectedTransfer) { this.expectedTransfer = expectedTransfer; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
