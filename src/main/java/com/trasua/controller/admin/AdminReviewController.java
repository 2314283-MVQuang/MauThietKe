package com.trasua.controller.admin;

import com.trasua.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quản lý duyệt đánh giá: ẩn nhận xét không phù hợp hoặc xoá hẳn. */
@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reviews", reviewService.allReviews());
        return "admin/reviews";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes ra) {
        reviewService.toggleVisible(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái hiển thị");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        reviewService.deleteByAdmin(id);
        ra.addFlashAttribute("success", "Đã xoá đánh giá");
        return "redirect:/admin/reviews";
    }
}
