package com.trasua.controller.admin;

import com.trasua.service.HoursChart;
import com.trasua.service.PayrollService;
import com.trasua.service.PayrollSummary;
import com.trasua.web.MonthParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.YearMonth;
import java.util.List;

/**
 * BẢNG LƯƠNG THÁNG (chỉ ADMIN): quản lý biết tháng này phải trả bao nhiêu cho nhân viên,
 * thống kê giờ làm, đánh dấu đã trả (chốt số liệu), xuất CSV.
 */
@Controller
@RequestMapping("/admin/payroll")
public class AdminPayrollController {

    private final PayrollService payrollService;

    public AdminPayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public String payroll(@RequestParam(value = "month", required = false) String monthParam, Model model) {
        YearMonth month = MonthParam.parse(monthParam);
        List<PayrollSummary> summaries = payrollService.calculateAll(month);
        BigDecimal paid = summaries.stream().filter(PayrollSummary::isPaid)
                .map(PayrollSummary::getPayable).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = payrollService.totalPayable(summaries);
        model.addAttribute("summaries", summaries);
        model.addAttribute("totalPayable", total);
        model.addAttribute("totalPaid", paid);
        model.addAttribute("totalUnpaid", total.subtract(paid));
        long totalMinutes = summaries.stream().mapToLong(PayrollSummary::getTotalMinutes).sum();
        model.addAttribute("totalHoursText", com.trasua.pattern.strategy.salary.PayrollFormat.hours(totalMinutes));
        model.addAttribute("totalShifts", summaries.stream().mapToInt(PayrollSummary::getShiftCount).sum());
        model.addAttribute("staffBars", HoursChart.byStaff(summaries));
        model.addAttribute("dayBars", HoursChart.byDay(summaries.stream()
                .flatMap(s -> s.getShifts().stream()).toList()));
        AdminStaffController.addMonth(model, month);
        return "admin/payroll";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam("staffId") Long staffId, @RequestParam("month") String month,
                      Principal principal, RedirectAttributes ra) {
        try {
            var record = payrollService.markPaid(staffId, MonthParam.parse(month), principal.getName());
            ra.addFlashAttribute("success", "Đã chốt & trả lương cho " + record.getStaff().getFullName()
                    + ": " + String.format("%,.0fđ", record.getAmount()).replace(',', '.'));
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/payroll?month=" + MonthParam.parse(month);
    }

    @PostMapping("/unpay")
    public String unpay(@RequestParam("staffId") Long staffId, @RequestParam("month") String month,
                        RedirectAttributes ra) {
        payrollService.unmarkPaid(staffId, MonthParam.parse(month));
        ra.addFlashAttribute("success", "Đã huỷ chốt lương - số tiền sẽ tính lại theo ca làm hiện tại");
        return "redirect:/admin/payroll?month=" + MonthParam.parse(month);
    }

    /** Xuất bảng lương ra CSV (mở bằng Excel). */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(value = "month", required = false) String monthParam) {
        YearMonth month = MonthParam.parse(monthParam);
        StringBuilder csv = new StringBuilder("﻿");   // BOM để Excel đọc đúng tiếng Việt
        csv.append("Nhân viên,Tên đăng nhập,Loại,Số ca,Tổng giờ,Lương/giờ,Lương phải trả,Trạng thái\n");
        for (PayrollSummary s : payrollService.calculateAll(month)) {
            csv.append(quote(s.getStaff().getFullName())).append(',')
                    .append(s.getStaff().getUsername()).append(',')
                    .append(s.getStaff().getEmploymentType().getLabel()).append(',')
                    .append(s.getShiftCount()).append(',')
                    .append(s.getTotalHours().toPlainString()).append(',')
                    .append(s.getStaff().getHourlyRate().toBigInteger()).append(',')
                    .append(s.getPayable().toBigInteger()).append(',')
                    .append(s.isPaid() ? "Đã trả" : "Chưa trả").append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bang-luong-" + month + ".csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String quote(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
    }
}
