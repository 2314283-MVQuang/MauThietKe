package com.trasua.service;

import com.trasua.entity.PayrollRecord;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.entity.WorkShift;
import com.trasua.pattern.strategy.salary.PayrollCalculator;
import com.trasua.pattern.strategy.salary.SalaryBreakdown;
import com.trasua.pattern.strategy.salary.SalaryInput;
import com.trasua.pattern.strategy.salary.SalaryStrategy;
import com.trasua.pattern.strategy.salary.SalaryStrategyFactory;
import com.trasua.pattern.strategy.salary.ShiftInfo;
import com.trasua.repository.PayrollRecordRepository;
import com.trasua.repository.UserRepository;
import com.trasua.repository.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Tính lương nhân viên theo tháng.
 * Dùng chung cho trang Chấm công của nhân viên và trang Bảng lương của quản lý,
 * nên hai bên luôn ra cùng một con số.
 */
@Service
public class PayrollService {

    private final WorkShiftRepository shiftRepository;
    private final PayrollRecordRepository recordRepository;
    private final UserRepository userRepository;

    public PayrollService(WorkShiftRepository shiftRepository, PayrollRecordRepository recordRepository,
                          UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    /** Các ca của nhân viên bắt đầu trong tháng. */
    public List<WorkShift> shiftsOf(User staff, YearMonth month) {
        LocalDateTime from = month.atDay(1).atStartOfDay();
        LocalDateTime to = month.plusMonths(1).atDay(1).atStartOfDay();
        return shiftRepository.findByStaffAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInAsc(staff, from, to);
    }

    /** Bảng lương 1 nhân viên trong 1 tháng. */
    public PayrollSummary calculate(User staff, YearMonth month) {
        List<WorkShift> shifts = shiftsOf(staff, month);
        List<ShiftInfo> closed = shifts.stream()
                .filter(s -> !s.isOpen())
                .map(WorkShift::toShiftInfo)
                .toList();
        SalaryInput input = new SalaryInput(closed, staff.getHourlyRate(), staff.getBaseSalary());

        // STRATEGY: chọn cách tính theo loại nhân viên (đã khoác sẵn DECORATOR phụ cấp/thưởng)
        SalaryStrategy strategy = SalaryStrategyFactory.forType(staff.getEmploymentType());
        PayrollCalculator calculator = new PayrollCalculator(strategy);   // Context
        SalaryBreakdown breakdown = calculator.calculate(input);

        WorkShift open = shifts.stream().filter(WorkShift::isOpen).findFirst().orElse(null);
        PayrollRecord paid = recordRepository.findByStaffAndPayrollMonth(staff, month.toString()).orElse(null);
        return new PayrollSummary(staff, month, shifts, closed.size(), input.totalMinutes(),
                breakdown, strategy.getName(), open, paid);
    }

    /** Bảng lương tất cả nhân viên trong tháng (cho quản lý). */
    public List<PayrollSummary> calculateAll(YearMonth month) {
        return userRepository.findByRoleOrderByFullNameAsc(Role.STAFF).stream()
                .map(staff -> calculate(staff, month))
                .toList();
    }

    /** Tổng tiền lương phải trả trong tháng. */
    public BigDecimal totalPayable(List<PayrollSummary> summaries) {
        return summaries.stream().map(PayrollSummary::getPayable).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Quản lý xác nhận đã trả lương -> chốt số liệu tháng đó. */
    @Transactional
    public PayrollRecord markPaid(Long staffId, YearMonth month, String adminUsername) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên #" + staffId));
        if (recordRepository.findByStaffAndPayrollMonth(staff, month.toString()).isPresent()) {
            throw new IllegalArgumentException("Lương tháng " + month + " của " + staff.getFullName() + " đã được chốt");
        }
        if (!month.isBefore(YearMonth.now().plusMonths(1))) {
            throw new IllegalArgumentException("Không thể trả lương cho tháng chưa tới");
        }
        PayrollSummary summary = calculate(staff, month);
        if (summary.getOpenShift() != null) {
            throw new IllegalArgumentException(staff.getFullName() + " còn ca chưa kết thúc - kết ca hoặc sửa giờ ra trước khi chốt lương");
        }
        PayrollRecord record = new PayrollRecord();
        record.setStaff(staff);
        record.setPayrollMonth(month.toString());
        record.setShiftCount(summary.getShiftCount());
        record.setTotalMinutes(summary.getTotalMinutes());
        record.setAmount(summary.getAmount());
        record.setPaidAt(LocalDateTime.now());
        record.setPaidBy(adminUsername);
        return recordRepository.save(record);
    }

    /** Huỷ chốt (bấm nhầm) -> lương tính lại theo dữ liệu hiện tại. */
    @Transactional
    public void unmarkPaid(Long staffId, YearMonth month) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên #" + staffId));
        recordRepository.findByStaffAndPayrollMonth(staff, month.toString()).ifPresent(recordRepository::delete);
    }

    public boolean isPaid(User staff, YearMonth month) {
        return recordRepository.findByStaffAndPayrollMonth(staff, month.toString()).isPresent();
    }
}
