package com.trasua.controller;

import com.trasua.entity.Category;
import com.trasua.entity.Product;
import com.trasua.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NHÂN VIÊN đánh dấu món nước (và topping) CÒN / HẾT ngay tại quầy.
 * Món hết: khách vẫn thấy trên menu nhưng mờ + nhãn "Tạm hết", không thêm vào giỏ / đặt hàng được.
 */
@Controller
@RequestMapping("/staff/menu")
public class StaffMenuController {

    private final CatalogService catalogService;

    public StaffMenuController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public String page(Model model) {
        List<Product> products = catalogService.allProducts();
        Map<Category, List<Product>> byCategory = new LinkedHashMap<>();
        for (Product p : products) {
            byCategory.computeIfAbsent(p.getCategory(), c -> new java.util.ArrayList<>()).add(p);
        }
        model.addAttribute("byCategory", byCategory);
        model.addAttribute("soldOutCount", products.stream().filter(p -> !p.isAvailable()).count());
        model.addAttribute("productCount", products.size());
        model.addAttribute("toppings", catalogService.allToppings());
        return "staff/menu";
    }

    @PostMapping("/products/{id}")
    public String setProduct(@PathVariable("id") Long id, @RequestParam("available") boolean available,
                             Principal principal, RedirectAttributes ra) {
        Product p = catalogService.setProductAvailable(id, available, principal.getName());
        ra.addFlashAttribute("success", (available ? "Đã mở bán lại: " : "Đã đánh dấu HẾT: ") + p.getName());
        return "redirect:/staff/menu#p" + id;
    }

    @PostMapping("/toppings/{id}")
    public String setTopping(@PathVariable("id") Long id, @RequestParam("available") boolean available,
                             RedirectAttributes ra) {
        var t = catalogService.setToppingAvailable(id, available);
        ra.addFlashAttribute("success", (available ? "Topping còn: " : "Topping HẾT: ") + t.getName());
        return "redirect:/staff/menu#toppings";
    }

    /** Đầu ca: mở bán lại tất cả món đang hết. */
    @PostMapping("/reopen-all")
    public String reopenAll(Principal principal, RedirectAttributes ra) {
        int n = catalogService.reopenAllProducts(principal.getName());
        ra.addFlashAttribute("success", n == 0 ? "Không có món nào đang hết" : "Đã mở bán lại " + n + " món");
        return "redirect:/staff/menu";
    }
}
