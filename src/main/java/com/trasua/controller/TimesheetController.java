package com.trasua.controller;

import com.trasua.entity.User;
import com.trasua.entity.WorkShift;
import com.trasua.service.PayrollService;
import com.trasua.service.PayrollSummary;
import com.trasua.service.TimesheetService;
import com.trasua.service.UserService;
import com.trasua.web.MonthParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Chấm công & xem lương của NHÂN VIÊN (role STAFF).
 * Nhân viên chỉ thấy ca của chính mình (lấy theo tài khoản đang đăng nhập).
 */
@Controller
@RequestMapping("/staff/timesheet")
public class TimesheetController {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final TimesheetService timesheetService;
    private final PayrollService payrollService;
    private final UserService userService;

    public TimesheetController(TimesheetService timesheetService, PayrollService payrollService,
                               UserService userService) {
        this.timesheetService = timesheetService;
        this.payrollService = payrollService;
        this.userService = userService;
    }

    @GetMapping
    public String timesheet(@RequestParam(value = "month", required = false) String monthParam,
                            Principal principal, Model model) {
        User me = userService.getByUsername(principal.getName());
        YearMonth month = MonthParam.parse(monthParam);
        PayrollSummary summary = payrollService.calculate(me, month);
        model.addAttribute("me", me);
        model.addAttribute("summary", summary);
        model.addAttribute("openShift", timesheetService.openShift(me).orElse(null));
        model.addAttribute("month", month.toString());
        model.addAttribute("monthLabel", MonthParam.label(month));
        model.addAttribute("prevMonth", month.minusMonths(1).toString());
        model.addAttribute("nextMonth", month.plusMonths(1).toString());
        model.addAttribute("isCurrentMonth", month.equals(YearMonth.now()));
        return "staff/timesheet";
    }

    @PostMapping("/check-in")
    public String checkIn(Principal principal, RedirectAttributes ra) {
        try {
            WorkShift shift = timesheetService.checkIn(userService.getByUsername(principal.getName()));
            ra.addFlashAttribute("success", "Đã vào ca lúc " + shift.getCheckIn().format(HM) + ". Làm việc vui nhé!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/timesheet";
    }

    @PostMapping("/check-out")
    public String checkOut(Principal principal, RedirectAttributes ra) {
        try {
            WorkShift shift = timesheetService.checkOut(userService.getByUsername(principal.getName()));
            ra.addFlashAttribute("success", "Đã kết ca lúc " + shift.getCheckOut().format(HM)
                    + " - ca này " + shift.getHoursText() + " giờ.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/timesheet";
    }
}
