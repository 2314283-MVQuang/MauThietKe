package com.trasua.entity;

import com.trasua.pattern.strategy.salary.PayrollFormat;
import com.trasua.pattern.strategy.salary.ShiftInfo;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/** Một ca làm của nhân viên (chấm công Vào ca / Kết ca). */
@Entity
@Table(name = "work_shifts", indexes = {
        @Index(name = "idx_shift_staff_checkin", columnList = "staff_id, check_in")
})
public class WorkShift {

    /** Ca dài hơn 12 giờ (hoặc quên kết ca) sẽ bị đánh dấu cảnh báo. */
    public static final long WARNING_MINUTES = 12 * 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "check_in", nullable = false)
    private LocalDateTime checkIn;

    /** null = đang trong ca. */
    @Column(name = "check_out")
    private LocalDateTime checkOut;

    /** Đơn giá/giờ chụp lại lúc vào ca - đổi lương sau này không làm sai số tiền ca cũ. */
    @Column(name = "hourly_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "note", length = 255)
    private String note;

    /** Quản lý đã sửa giờ ca này (tên đăng nhập) + lý do. */
    @Column(name = "edited_by", length = 50)
    private String editedBy;

    @Column(name = "edit_reason", length = 255)
    private String editReason;

    public WorkShift() {
    }

    public WorkShift(User staff, LocalDateTime checkIn, BigDecimal hourlyRate) {
        this.staff = staff;
        this.checkIn = checkIn;
        this.hourlyRate = hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
    }

    public boolean isOpen() {
        return checkOut == null;
    }

    /** Số phút đã làm (ca đang mở thì tính đến hiện tại). */
    public long getMinutes() {
        LocalDateTime end = checkOut != null ? checkOut : LocalDateTime.now();
        return Math.max(0, Duration.between(checkIn, end).toMinutes());
    }

    public String getHoursText() {
        return PayrollFormat.hours(getMinutes());
    }

    /** Thành tiền của ca (làm tròn đến đồng). */
    public BigDecimal getAmount() {
        return hourlyRate.multiply(BigDecimal.valueOf(getMinutes()))
                .divide(BigDecimal.valueOf(60), 0, RoundingMode.HALF_UP);
    }

    public boolean isWarning() {
        return getMinutes() > WARNING_MINUTES;
    }

    /** "T4 14/10" - thứ + ngày của ca. */
    public String getDayLabel() {
        String[] names = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        return names[checkIn.getDayOfWeek().getValue() - 1] + " "
                + String.format("%02d/%02d", checkIn.getDayOfMonth(), checkIn.getMonthValue());
    }

    /** Giá trị cho ô nhập datetime-local (yyyy-MM-ddTHH:mm). */
    public String getCheckInInput() {
        return checkIn == null ? "" : checkIn.withSecond(0).withNano(0).toString();
    }

    public String getCheckOutInput() {
        return checkOut == null ? "" : checkOut.withSecond(0).withNano(0).toString();
    }

    public boolean isWeekend() {
        return checkIn.getDayOfWeek().getValue() >= 6;
    }

    public boolean isEdited() {
        return editedBy != null;
    }

    /** Chuyển sang dữ liệu thuần cho Strategy tính lương (chỉ ca đã kết thúc). */
    public ShiftInfo toShiftInfo() {
        return new ShiftInfo(checkIn, checkOut, hourlyRate);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getEditedBy() { return editedBy; }
    public void setEditedBy(String editedBy) { this.editedBy = editedBy; }
    public String getEditReason() { return editReason; }
    public void setEditReason(String editReason) { this.editReason = editReason; }
}
