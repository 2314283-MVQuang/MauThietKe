package com.trasua.controller;

import com.trasua.entity.User;
import com.trasua.service.ReviewService;
import com.trasua.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Khách hàng gửi / sửa / xoá đánh giá sao và nhận xét món. */
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @PostMapping
    public String save(@RequestParam("productId") Long productId,
                       @RequestParam(value = "rating", defaultValue = "0") int rating,
                       @RequestParam(value = "comment", required = false) String comment,
                       Principal principal, RedirectAttributes ra) {
        User user = userService.getByUsername(principal.getName());
        try {
            boolean existed = reviewService.findMine(productId, user).isPresent();
            reviewService.saveReview(user, productId, rating, comment);
            ra.addFlashAttribute("success", existed ? "Đã cập nhật đánh giá của bạn" : "Cảm ơn bạn đã đánh giá!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId + "#reviews";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("productId") Long productId, Principal principal, RedirectAttributes ra) {
        reviewService.deleteMine(userService.getByUsername(principal.getName()), productId);
        ra.addFlashAttribute("success", "Đã xoá đánh giá của bạn");
        return "redirect:/products/" + productId + "#reviews";
    }
}
