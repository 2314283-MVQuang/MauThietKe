package com.trasua.controller.admin;

import com.trasua.entity.FeedbackStatus;
import com.trasua.service.CatalogService;
import com.trasua.service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** QUẢN LÝ đọc góp ý món của khách, trả lời và cập nhật tiến trình cải thiện. */
@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;
    private final CatalogService catalogService;

    public AdminFeedbackController(FeedbackService feedbackService, CatalogService catalogService) {
        this.feedbackService = feedbackService;
        this.catalogService = catalogService;
    }

    @GetMapping
    public String list(@RequestParam(value = "status", required = false) FeedbackStatus status,
                       @RequestParam(value = "productId", required = false) Long productId,
                       Model model) {
        model.addAttribute("feedbacks", feedbackService.all(status, productId));
        model.addAttribute("statusCounts", feedbackService.countByStatus());
        model.addAttribute("topProducts", feedbackService.topProducts(5));
        model.addAttribute("statuses", FeedbackStatus.values());
        model.addAttribute("products", catalogService.allProducts());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProductId", productId);
        return "admin/feedback";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable("id") Long id,
                        @RequestParam(value = "reply", required = false) String reply,
                        @RequestParam(value = "status", required = false) FeedbackStatus status,
                        @RequestParam(value = "back", required = false) String back,
                        Principal principal, RedirectAttributes ra) {
        try {
            feedbackService.reply(id, reply, status, principal.getName());
            ra.addFlashAttribute("success", "Đã cập nhật góp ý #" + id);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        String query = back != null && back.matches("[a-zA-Z0-9=&_]*") ? back : "";
        return "redirect:/admin/feedback" + (query.isEmpty() ? "" : "?" + query) + "#f" + id;
    }
}
