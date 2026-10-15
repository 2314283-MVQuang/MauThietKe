package com.trasua.config;

import com.trasua.entity.EmploymentType;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.entity.WorkShift;
import com.trasua.repository.UserRepository;
import com.trasua.repository.WorkShiftRepository;
import com.trasua.service.PayrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Dữ liệu mẫu cho phần CHẤM CÔNG & BẢNG LƯƠNG (chạy sau DataSeeder, khi ứng dụng đã sẵn sàng):
 *   - nhanvien  : bán thời gian, 25.000đ/giờ
 *   - nhanvien2 : toàn thời gian, lương cứng 6.500.000đ, 32.000đ/giờ (mật khẩu nv123)
 *   - ca làm của tháng trước (đã trả lương) và tháng này (đến hôm qua)
 * Chỉ tạo ca khi bảng work_shifts còn trống.
 */
@Component
public class PayrollDemoSeeder {

    private static final Logger log = LoggerFactory.getLogger(PayrollDemoSeeder.class);

    private final UserRepository userRepository;
    private final WorkShiftRepository shiftRepository;
    private final PayrollService payrollService;
    private final PasswordEncoder passwordEncoder;

    public PayrollDemoSeeder(UserRepository userRepository, WorkShiftRepository shiftRepository,
                             PayrollService payrollService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
        this.payrollService = payrollService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        // Nhân viên cũ chưa có lương/giờ -> mặc định bán thời gian 25.000đ/giờ
        for (User staff : userRepository.findByRoleOrderByFullNameAsc(Role.STAFF)) {
            if (staff.getHourlyRate().signum() == 0) {
                staff.setEmploymentType(EmploymentType.PART_TIME);
                staff.setHourlyRate(BigDecimal.valueOf(25_000));
                userRepository.save(staff);
            }
        }
        if (shiftRepository.count() > 0) {
            return;
        }
        User partTime = userRepository.findByUsername("nhanvien").orElse(null);
        if (partTime == null || partTime.getRole() != Role.STAFF) {
            return;
        }
        User fullTime = userRepository.findByUsername("nhanvien2").orElseGet(() -> {
            User u = new User("nhanvien2", passwordEncoder.encode("nv123"), "Lê Thị Hoa", Role.STAFF);
            u.setPhone("0901000006");
            u.setEmploymentType(EmploymentType.FULL_TIME);
            u.setHourlyRate(BigDecimal.valueOf(32_000));
            u.setBaseSalary(BigDecimal.valueOf(6_500_000));
            return userRepository.save(u);
        });

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        int count = 0;
        for (LocalDate day = lastMonth.atDay(1); !day.isAfter(yesterday); day = day.plusDays(1)) {
            DayOfWeek dow = day.getDayOfWeek();
            boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            // Bán thời gian: nghỉ Thứ 2 & Thứ 5; ngày thường ca tối 4,5 giờ, cuối tuần ca sáng 5 giờ
            if (dow != DayOfWeek.MONDAY && dow != DayOfWeek.THURSDAY) {
                count += weekend ? shift(partTime, day, 8, 0, 13, 0) : shift(partTime, day, 17, 0, 21, 30);
            }
            // Toàn thời gian: Thứ 2 - Thứ 7, 8 giờ/ngày; Thứ 7 làm thêm 1 giờ; nghỉ Chủ nhật
            if (dow != DayOfWeek.SUNDAY) {
                count += dow == DayOfWeek.SATURDAY ? shift(fullTime, day, 7, 0, 16, 0) : shift(fullTime, day, 7, 0, 15, 0);
            }
        }
        // Tháng trước đã trả lương xong
        payrollService.markPaid(partTime.getId(), lastMonth, "admin");
        payrollService.markPaid(fullTime.getId(), lastMonth, "admin");
        log.info("Đã tạo {} ca làm mẫu và chốt lương tháng {}", count, lastMonth);
    }

    private int shift(User staff, LocalDate day, int h1, int m1, int h2, int m2) {
        WorkShift s = new WorkShift(staff, day.atTime(h1, m1), staff.getHourlyRate());
        s.setCheckOut(day.atTime(h2, m2));
        shiftRepository.save(s);
        return 1;
    }
}
