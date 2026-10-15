package com.trasua.service;

import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.entity.WorkShift;
import com.trasua.repository.UserRepository;
import com.trasua.repository.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Chấm công: nhân viên Vào ca / Kết ca; quản lý sửa hoặc thêm ca bị sai. */
@Service
public class TimesheetService {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm dd/MM");
    private static final long MAX_SHIFT_HOURS = 16;

    private final WorkShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final PayrollService payrollService;

    public TimesheetService(WorkShiftRepository shiftRepository, UserRepository userRepository,
                            PayrollService payrollService) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
        this.payrollService = payrollService;
    }

    public Optional<WorkShift> openShift(User staff) {
        return shiftRepository.findFirstByStaffAndCheckOutIsNullOrderByCheckInDesc(staff);
    }

    @Transactional
    public WorkShift checkIn(User staff) {
        requireStaff(staff);
        openShift(staff).ifPresent(s -> {
            throw new IllegalStateException("Bạn đang trong ca từ " + s.getCheckIn().format(HM) + " - hãy kết ca trước");
        });
        if (staff.getHourlyRate().signum() <= 0) {
            throw new IllegalStateException("Bạn chưa được quản lý khai báo lương theo giờ - liên hệ quản lý");
        }
        return shiftRepository.save(new WorkShift(staff, LocalDateTime.now().withSecond(0).withNano(0), staff.getHourlyRate()));
    }

    @Transactional
    public WorkShift checkOut(User staff) {
        WorkShift shift = openShift(staff)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa vào ca"));
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        shift.setCheckOut(now.isBefore(shift.getCheckIn()) ? shift.getCheckIn() : now);
        return shift;
    }

    /** Quản lý sửa giờ vào/ra của một ca (bắt buộc ghi lý do). */
    @Transactional
    public WorkShift editShift(Long shiftId, LocalDateTime checkIn, LocalDateTime checkOut,
                               String reason, String adminUsername) {
        WorkShift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ca #" + shiftId));
        ensureMonthOpen(shift.getStaff(), YearMonth.from(shift.getCheckIn()));
        validate(checkIn, checkOut, reason);
        ensureMonthOpen(shift.getStaff(), YearMonth.from(checkIn));
        shift.setCheckIn(checkIn);
        shift.setCheckOut(checkOut);
        shift.setEditedBy(adminUsername);
        shift.setEditReason(reason.trim());
        return shift;
    }

    /** Quản lý thêm ca bị thiếu (nhân viên quên chấm công). Đơn giá = lương/giờ hiện tại. */
    @Transactional
    public WorkShift addShift(Long staffId, LocalDateTime checkIn, LocalDateTime checkOut,
                              String reason, String adminUsername) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên #" + staffId));
        requireStaff(staff);
        validate(checkIn, checkOut, reason);
        ensureMonthOpen(staff, YearMonth.from(checkIn));
        WorkShift shift = new WorkShift(staff, checkIn, staff.getHourlyRate());
        shift.setCheckOut(checkOut);
        shift.setEditedBy(adminUsername);
        shift.setEditReason(reason.trim());
        return shiftRepository.save(shift);
    }

    private void ensureMonthOpen(User staff, YearMonth month) {
        if (payrollService.isPaid(staff, month)) {
            throw new IllegalArgumentException("Lương tháng " + month + " đã chốt - huỷ chốt trước khi sửa ca");
        }
    }

    private static void validate(LocalDateTime checkIn, LocalDateTime checkOut, String reason) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Vui lòng nhập đủ giờ vào và giờ ra");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Giờ ra phải sau giờ vào");
        }
        if (Duration.between(checkIn, checkOut).toHours() >= MAX_SHIFT_HOURS) {
            throw new IllegalArgumentException("Một ca không được dài quá " + MAX_SHIFT_HOURS + " giờ");
        }
        if (checkOut.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalArgumentException("Giờ ra không được ở tương lai");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Vui lòng ghi lý do sửa / thêm ca");
        }
    }

    private static void requireStaff(User user) {
        if (user.getRole() != Role.STAFF) {
            throw new IllegalStateException("Chỉ nhân viên mới chấm công");
        }
    }
}
