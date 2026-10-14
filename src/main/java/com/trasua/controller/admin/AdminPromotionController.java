package com.trasua.controller.admin;

import com.trasua.entity.Promotion;
import com.trasua.entity.PromotionType;
import com.trasua.pattern.strategy.discount.DiscountStrategyFactory;
import com.trasua.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

/** Quản lý mã khuyến mãi - mỗi loại khuyến mãi ứng với một DiscountStrategy. */
@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {

    private final PromotionService promotionService;

    public AdminPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public String list(@RequestParam(value = "edit", required = false) Long editId, Model model) {
        var promotions = promotionService.findAll();
        // Hiển thị cột "Chiến lược" = describe() của Strategy tương ứng
        Map<Long, String> strategyText = new LinkedHashMap<>();
        for (Promotion p : promotions) {
            try {
                strategyText.put(p.getId(), DiscountStrategyFactory.fromPromotion(p).describe());
            } catch (IllegalArgumentException e) {
                strategyText.put(p.getId(), "Cấu hình lỗi: " + e.getMessage());
            }
        }
        model.addAttribute("promotions", promotions);
        model.addAttribute("strategyText", strategyText);
        model.addAttribute("types", PromotionType.values());
        model.addAttribute("form", editId == null ? new Promotion() : promotionService.getById(editId));
        return "admin/promotions";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") Promotion form, RedirectAttributes ra) {
        try {
            promotionService.save(form);
            ra.addFlashAttribute("success", "Đã lưu mã khuyến mãi");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes ra) {
        promotionService.toggle(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        promotionService.delete(id);
        ra.addFlashAttribute("success", "Đã xoá mã khuyến mãi");
        return "redirect:/admin/promotions";
    }
}
