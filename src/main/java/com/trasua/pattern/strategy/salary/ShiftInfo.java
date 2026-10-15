package com.trasua.pattern.strategy.salary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Dữ liệu 1 ca làm (đã kết ca) dùng để tính lương - lớp Java thuần, không phụ thuộc JPA
 * nên kiểm thử được độc lập. Lấy từ entity WorkShift (xem PayrollService).
 *
 * @param checkIn    giờ vào ca
 * @param checkOut   giờ kết ca
 * @param hourlyRate đơn giá/giờ TẠI THỜI ĐIỂM làm ca (đổi lương sau này không ảnh hưởng ca cũ)
 */
public record ShiftInfo(LocalDateTime checkIn, LocalDateTime checkOut, BigDecimal hourlyRate) {

    public ShiftInfo {
        if (checkIn == null || checkOut == null || checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException("Ca làm không hợp lệ: giờ ra phải sau giờ vào");
        }
        hourlyRate = hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
    }

    /** Số phút làm việc của ca. */
    public long minutes() {
        return Duration.between(checkIn, checkOut).toMinutes();
    }

    /** Thành tiền của ca = phút x đơn giá / 60 (chưa làm tròn). */
    public BigDecimal amount() {
        return hourlyRate.multiply(BigDecimal.valueOf(minutes()))
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
    }

    /** Ca bắt đầu vào Thứ 7 hoặc Chủ nhật. */
    public boolean isWeekend() {
        DayOfWeek d = checkIn.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }
}
