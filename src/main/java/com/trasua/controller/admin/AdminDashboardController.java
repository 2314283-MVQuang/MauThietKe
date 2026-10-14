package com.trasua.controller.admin;

import com.trasua.entity.OrderStatus;
import com.trasua.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang tổng quan / thống kê doanh thu của quản lý. */
@Controller
public class AdminDashboardController {

    private final StatisticsService statisticsService;

    public AdminDashboardController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("revenueToday", statisticsService.revenueToday());
        model.addAttribute("revenueMonth", statisticsService.revenueThisMonth());
        var statusCounts = statisticsService.countByStatus();
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("pendingCount", statusCounts.get(OrderStatus.CHO_XAC_NHAN));
        model.addAttribute("days", statisticsService.last7Days());
        model.addAttribute("bestSellers", statisticsService.bestSellers(5));
        model.addAttribute("recentOrders", statisticsService.recentOrders());
        model.addAttribute("productCount", statisticsService.productCount());
        model.addAttribute("customerCount", statisticsService.customerCount());
        model.addAttribute("staffCount", statisticsService.staffCount());
        return "admin/dashboard";
    }
}
