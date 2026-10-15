package com.trasua.controller;

import com.trasua.entity.FeedbackTopic;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.service.CatalogService;
import com.trasua.service.FeedbackService;
import com.trasua.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** KHÁCH HÀNG gửi góp ý món & xem quán đã phản hồi / cải thiện thế nào. */
@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CatalogService catalogService;
    private final UserService userService;

    public FeedbackController(FeedbackService feedbackService, CatalogService catalogService, UserService userService) {
        this.feedbackService = feedbackService;
        this.catalogService = catalogService;
        this.userService = userService;
    }

    @GetMapping
    public String page(@RequestParam(value = "productId", required = false) Long productId,
                       Principal principal, Model model) {
        User me = userService.getByUsername(principal.getName());
        if (me.getRole() == Role.ADMIN) {
            return "redirect:/admin/feedback";
        }
        model.addAttribute("isCustomer", me.getRole() == Role.CUSTOMER);
        model.addAttribute("products", catalogService.allProducts());
        model.addAttribute("topics", FeedbackTopic.values());
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("mine", feedbackService.mine(me));
        model.addAttribute("improved", feedbackService.improvedHighlights());
        return "feedback/index";
    }

    @PostMapping
    public String submit(@RequestParam(value = "productId", required = false) Long productId,
                         @RequestParam(value = "topic", required = false) FeedbackTopic topic,
                         @RequestParam(value = "content", required = false) String content,
                         Principal principal, RedirectAttributes ra) {
        try {
            feedbackService.submit(userService.getByUsername(principal.getName()), productId, topic, content);
            ra.addFlashAttribute("success", "Cảm ơn bạn! Quán đã nhận góp ý và sẽ phản hồi sớm.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("draft", content);
        }
        return "redirect:/feedback" + (productId == null ? "" : "?productId=" + productId);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, Principal principal, RedirectAttributes ra) {
        try {
            feedbackService.deleteOwn(id, userService.getByUsername(principal.getName()));
            ra.addFlashAttribute("success", "Đã xoá góp ý");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/feedback";
    }
}
