package com.trasua.controller;

import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.service.DailyReportService;
import com.trasua.service.OrderService;
import com.trasua.web.DayPicker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

/**
 * Nhân viên (và quản lý) xử lý đơn hàng THEO NGÀY: xác nhận, pha chế, giao, hoàn thành, huỷ.
 * Mỗi thao tác ghi lại ai làm để cuối ngày quản lý đối soát.
 */
@Controller
@RequestMapping("/staff/orders")
public class StaffController {

    private final OrderService orderService;
    private final DailyReportService dailyReportService;

    public StaffController(OrderService orderService, DailyReportService dailyReportService) {
        this.orderService = orderService;
        this.dailyReportService = dailyReportService;
    }

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) OrderStatus status,
                       @RequestParam(value = "date", required = false) String dateParam,
                       Model model) {
        LocalDate date = DayPicker.parse(dateParam);
        String statusQuery = status == null ? "" : "&status=" + status.name();
        model.addAttribute("orders", orderService.findByDate(date, status));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("picker", new DayPicker(date, orderService.countByDayOfWeek(date), statusQuery));
        model.addAttribute("report", dailyReportService.report(date));
        model.addAttribute("olderUnfinished", orderService.countUnfinishedBefore(date));
        model.addAttribute("olderUnfinishedDate", orderService.oldestUnfinishedDate(date));
        model.addAttribute("dateQuery", "date=" + date);
        return "staff/orders";
    }

    @PostMapping("/{id}/next")
    public String next(@PathVariable("id") Long id,
                       @RequestParam(value = "back", required = false) String back,
                       Principal principal, RedirectAttributes ra) {
        Order order = null;
        try {
            order = orderService.moveToNextStatus(id, principal.getName());
            ra.addFlashAttribute("success", "Đơn " + order.getOrderCode() + " -> " + order.getStatus().getLabel());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return redirect(back, id);
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id,
                         @RequestParam(value = "back", required = false) String back,
                         Principal principal, RedirectAttributes ra) {
        try {
            orderService.cancelByStaff(id, principal.getName());
            ra.addFlashAttribute("success", "Đã huỷ đơn");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return redirect(back, id);
    }

    @PostMapping("/{id}/paid")
    public String confirmPaid(@PathVariable("id") Long id,
                              @RequestParam(value = "back", required = false) String back,
                              Principal principal, RedirectAttributes ra) {
        try {
            orderService.confirmPayment(id, principal.getName());
            ra.addFlashAttribute("success", "Đã xác nhận thanh toán");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return redirect(back, id);
    }

    /**
     * Quay lại trang chi tiết đơn nếu thao tác từ đó; ngược lại về danh sách đúng NGÀY của đơn
     * (back có dạng "date=2026-10-13&status=...").
     */
    private String redirect(String back, Long id) {
        if ("detail".equals(back)) {
            return "redirect:/orders/" + id;
        }
        if (back != null && back.matches("date=\\d{4}-\\d{2}-\\d{2}(&status=[A-Z_]+)?")) {
            return "redirect:/staff/orders?" + back;
        }
        return "redirect:/staff/orders";
    }
}
