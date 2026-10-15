package com.trasua.controller.admin;

import com.trasua.entity.OrderStatus;
import com.trasua.service.PayrollService;
import com.trasua.service.PayrollSummary;
import com.trasua.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang tổng quan / thống kê doanh thu của quản lý. */
@Controller
public class AdminDashboardController {

    private final StatisticsService statisticsService;
    private final PayrollService payrollService;

    public AdminDashboardController(StatisticsService statisticsService, PayrollService payrollService) {
        this.statisticsService = statisticsService;
        this.payrollService = payrollService;
    }

    @GetMapping("/admin")
    public String dashboard(@org.springframework.web.bind.annotation.RequestParam(value = "bs", required = false) String bsMode,
                            @org.springframework.web.bind.annotation.RequestParam(value = "bsDate", required = false) String bsDate,
                            Model model) {
        model.addAttribute("revenueToday", statisticsService.revenueToday());
        model.addAttribute("revenueMonth", statisticsService.revenueThisMonth());
        var statusCounts = statisticsService.countByStatus();
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("pendingCount", statusCounts.get(OrderStatus.CHO_XAC_NHAN));
        model.addAttribute("days", statisticsService.last7Days());
        // Top bán chạy theo ngày / tuần / tháng / toàn thời gian
        com.trasua.web.PeriodFilter period = com.trasua.web.PeriodFilter.parse(bsMode, bsDate);
        var bestSellers = statisticsService.bestSellers(5, period.getFrom(), period.getTo());
        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("bsPeriod", period);
        model.addAttribute("bsMaxQty", bestSellers.stream().mapToLong(StatisticsService.BestSeller::quantity).max().orElse(1));
        model.addAttribute("bsTotalQty", bestSellers.stream().mapToLong(StatisticsService.BestSeller::quantity).sum());
        model.addAttribute("recentOrders", statisticsService.recentOrders());
        model.addAttribute("productCount", statisticsService.productCount());
        model.addAttribute("customerCount", statisticsService.customerCount());
        model.addAttribute("staffCount", statisticsService.staffCount());
        // Lương nhân viên tháng này (Thuận - dùng PayrollService của Tuấn)
        var payroll = payrollService.calculateAll(java.time.YearMonth.now());
        model.addAttribute("payrollTotal", payrollService.totalPayable(payroll));
        model.addAttribute("payrollHours", com.trasua.pattern.strategy.salary.PayrollFormat.hours(
                payroll.stream().mapToLong(PayrollSummary::getTotalMinutes).sum()));
        model.addAttribute("payrollWorking", payroll.stream().filter(p -> p.getOpenShift() != null).count());
        return "admin/dashboard";
    }
}
