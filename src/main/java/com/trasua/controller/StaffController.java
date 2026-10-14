package com.trasua.controller;

import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Nhân viên (và quản lý) xử lý đơn hàng: xác nhận, pha chế, giao, hoàn thành, huỷ. */
@Controller
@RequestMapping("/staff/orders")
public class StaffController {

    private final OrderService orderService;

    public StaffController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) OrderStatus status, Model model) {
        model.addAttribute("orders", orderService.findAll(status));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "staff/orders";
    }

    @PostMapping("/{id}/next")
    public String next(@PathVariable("id") Long id,
                       @RequestParam(value = "back", required = false) String back,
                       RedirectAttributes ra) {
        try {
            Order order = orderService.moveToNextStatus(id);
            ra.addFlashAttribute("success", "Đơn " + order.getOrderCode() + " -> " + order.getStatus().getLabel());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return redirect(back, id);
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id,
                         @RequestParam(value = "back", required = false) String back,
                         RedirectAttributes ra) {
        try {
            orderService.cancelByStaff(id);
            ra.addFlashAttribute("success", "Đã huỷ đơn");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return redirect(back, id);
    }

    @PostMapping("/{id}/paid")
    public String confirmPaid(@PathVariable("id") Long id,
                              @RequestParam(value = "back", required = false) String back,
                              RedirectAttributes ra) {
        try {
            orderService.confirmPayment(id);
            ra.addFlashAttribute("success", "Đã xác nhận thanh toán");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return redirect(back, id);
    }

    /** Quay lại trang chi tiết đơn nếu thao tác từ đó, ngược lại về danh sách. */
    private static String redirect(String back, Long id) {
        return "detail".equals(back) ? "redirect:/orders/" + id : "redirect:/staff/orders";
    }
}
