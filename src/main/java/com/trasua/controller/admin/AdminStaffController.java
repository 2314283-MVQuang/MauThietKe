package com.trasua.controller.admin;

import com.trasua.dto.StaffForm;
import com.trasua.entity.EmploymentType;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.service.HoursChart;
import com.trasua.service.PayrollService;
import com.trasua.service.PayrollSummary;
import com.trasua.service.TimesheetService;
import com.trasua.service.UserService;
import com.trasua.web.MonthParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * QUẢN LÝ NHÂN VIÊN (chỉ ADMIN): hồ sơ + lương/giờ, giờ làm trong tháng,
 * chi tiết ca làm, sửa / thêm ca khi nhân viên quên chấm công.
 * Số tiền lương lấy từ PayrollService (dùng chung với trang Chấm công của nhân viên).
 */
@Controller
@RequestMapping("/admin/staff")
public class AdminStaffController {

    private final UserService userService;
    private final PayrollService payrollService;
    private final TimesheetService timesheetService;

    public AdminStaffController(UserService userService, PayrollService payrollService,
                                TimesheetService timesheetService) {
        this.userService = userService;
        this.payrollService = payrollService;
        this.timesheetService = timesheetService;
    }

    @GetMapping
    public String list(@RequestParam(value = "month", required = false) String monthParam, Model model) {
        YearMonth month = MonthParam.parse(monthParam);
        List<PayrollSummary> summaries = payrollService.calculateAll(month);
        model.addAttribute("summaries", summaries);
        model.addAttribute("totalPayable", payrollService.totalPayable(summaries));
        long totalMinutes = summaries.stream().mapToLong(PayrollSummary::getTotalMinutes).sum();
        model.addAttribute("totalHoursText", com.trasua.pattern.strategy.salary.PayrollFormat.hours(totalMinutes));
        model.addAttribute("workingNow", summaries.stream().filter(s -> s.getOpenShift() != null).count());
        model.addAttribute("partTimeCount", summaries.stream()
                .filter(s -> s.getStaff().getEmploymentType() == EmploymentType.PART_TIME).count());
        addMonth(model, month);
        return "admin/staff";
    }

    @GetMapping("/new")
    public String create(Model model) {
        return form(new StaffForm(), model);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        return form(StaffForm.from(requireStaff(id)), model);
    }

    private String form(StaffForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("types", EmploymentType.values());
        return "admin/staff-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") StaffForm form, Model model, RedirectAttributes ra) {
        try {
            User saved = userService.saveStaff(form);
            ra.addFlashAttribute("success", "Đã lưu nhân viên " + saved.getFullName());
            return "redirect:/admin/staff";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return form(form, model);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(value = "month", required = false) String monthParam, Model model) {
        User staff = requireStaff(id);
        YearMonth month = MonthParam.parse(monthParam);
        PayrollSummary summary = payrollService.calculate(staff, month);
        model.addAttribute("staff", staff);
        model.addAttribute("summary", summary);
        model.addAttribute("dayBars", HoursChart.byDay(summary.getShifts()));
        addMonth(model, month);
        return "admin/staff-detail";
    }

    /** Sửa giờ vào / ra của 1 ca (VD nhân viên quên bấm Kết ca). */
    @PostMapping("/shifts/{shiftId}/edit")
    public String editShift(@PathVariable("shiftId") Long shiftId,
                            @RequestParam("staffId") Long staffId,
                            @RequestParam("checkIn") String checkIn,
                            @RequestParam("checkOut") String checkOut,
                            @RequestParam(value = "reason", required = false) String reason,
                            @RequestParam(value = "month", required = false) String month,
                            Principal principal, RedirectAttributes ra) {
        try {
            timesheetService.editShift(shiftId, parse(checkIn), parse(checkOut), reason, principal.getName());
            ra.addFlashAttribute("success", "Đã sửa ca làm");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff/" + staffId + (month == null ? "" : "?month=" + month);
    }

    /** Thêm ca bị thiếu. */
    @PostMapping("/{id}/shifts/add")
    public String addShift(@PathVariable("id") Long id,
                           @RequestParam("checkIn") String checkIn,
                           @RequestParam("checkOut") String checkOut,
                           @RequestParam(value = "reason", required = false) String reason,
                           Principal principal, RedirectAttributes ra) {
        String month = null;
        try {
            LocalDateTime in = parse(checkIn);
            month = YearMonth.from(in).toString();
            timesheetService.addShift(id, in, parse(checkOut), reason, principal.getName());
            ra.addFlashAttribute("success", "Đã thêm ca làm");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff/" + id + (month == null ? "" : "?month=" + month);
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, Principal principal, RedirectAttributes ra) {
        requireStaff(id);
        try {
            userService.toggleEnabled(id, principal.getName());
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái nhân viên");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    private User requireStaff(Long id) {
        User user = userService.getById(id);
        if (user.getRole() != Role.STAFF) {
            throw new com.trasua.service.NotFoundException("Tài khoản #" + id + " không phải nhân viên");
        }
        return user;
    }

    private static LocalDateTime parse(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày giờ không hợp lệ: " + value);
        }
    }

    static void addMonth(Model model, YearMonth month) {
        model.addAttribute("month", month.toString());
        model.addAttribute("monthLabel", MonthParam.label(month));
        model.addAttribute("prevMonth", month.minusMonths(1).toString());
        model.addAttribute("nextMonth", month.plusMonths(1).toString());
    }
}
