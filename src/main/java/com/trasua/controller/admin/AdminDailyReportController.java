package com.trasua.controller.admin;

import com.trasua.entity.DailyClosing;
import com.trasua.service.DailyReportService;
import com.trasua.service.OrderService;
import com.trasua.web.DayPicker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

/**
 * TỔNG KẾT HOÁ ĐƠN THEO NGÀY & CHỐT KÉT (chỉ ADMIN):
 * tiền theo hình thức thanh toán, theo người thu, đơn huỷ / chưa thu tiền,
 * nhập tiền mặt đếm được để so với hoá đơn.
 */
@Controller
@RequestMapping("/admin/daily-report")
public class AdminDailyReportController {

    private final DailyReportService dailyReportService;
    private final OrderService orderService;

    public AdminDailyReportController(DailyReportService dailyReportService, OrderService orderService) {
        this.dailyReportService = dailyReportService;
        this.orderService = orderService;
    }

    @GetMapping
    public String report(@RequestParam(value = "date", required = false) String dateParam, Model model) {
        LocalDate date = DayPicker.parse(dateParam);
        model.addAttribute("picker", new DayPicker(date, orderService.countByDayOfWeek(date), ""));
        model.addAttribute("report", dailyReportService.report(date));
        model.addAttribute("history", dailyReportService.recentClosings());
        return "admin/daily-report";
    }

    @PostMapping("/close")
    public String close(@RequestParam("date") String dateParam,
                        @RequestParam(value = "countedCash", required = false) BigDecimal countedCash,
                        @RequestParam(value = "note", required = false) String note,
                        Principal principal, RedirectAttributes ra) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateParam);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Ngày không hợp lệ");
            return "redirect:/admin/daily-report";
        }
        try {
            DailyClosing c = dailyReportService.close(date, countedCash, note, principal.getName());
            BigDecimal diff = c.getDifference();
            String result = diff.signum() == 0 ? "KHỚP với hoá đơn."
                    : (diff.signum() < 0 ? "THIẾU " : "THỪA ") + money(diff.abs()) + " so với hoá đơn!";
            if (diff.signum() == 0) {
                ra.addFlashAttribute("success", "Đã chốt két ngày " + date + ": " + result);
            } else {
                ra.addFlashAttribute("error", "Đã chốt két ngày " + date + ": " + result);
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/daily-report?date=" + date;
    }

    /** Huỷ biên bản chốt két (chốt nhầm) để chốt lại. */
    @PostMapping("/reopen")
    public String reopen(@RequestParam("date") String dateParam, RedirectAttributes ra) {
        LocalDate date = DayPicker.parse(dateParam);
        dailyReportService.reopen(date);
        ra.addFlashAttribute("success", "Đã huỷ chốt két ngày " + date);
        return "redirect:/admin/daily-report?date=" + date;
    }

    private static String money(BigDecimal v) {
        return String.format("%,.0fđ", v).replace(',', '.');
    }
}
