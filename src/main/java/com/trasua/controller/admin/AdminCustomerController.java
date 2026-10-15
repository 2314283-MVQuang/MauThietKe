package com.trasua.controller.admin;

import com.trasua.entity.User;
import com.trasua.pattern.strategy.tier.CustomerTier;
import com.trasua.pattern.strategy.tier.CustomerTierClassifier;
import com.trasua.repository.ReviewRepository;
import com.trasua.service.CustomerService;
import com.trasua.service.CustomerSummary;
import com.trasua.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** QUẢN LÝ KHÁCH HÀNG (chỉ ADMIN): danh sách, tìm kiếm, xếp hạng, lịch sử mua, khoá/mở. */
@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final CustomerService customerService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;

    public AdminCustomerController(CustomerService customerService, UserService userService,
                                   ReviewRepository reviewRepository) {
        this.customerService = customerService;
        this.userService = userService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "sort", required = false, defaultValue = "chi-tieu") String sort,
                       @RequestParam(value = "xepHang", required = false) String tierKey,
                       Model model) {
        // STRATEGY: chọn tiêu chí xếp hạng theo tham số ?xepHang=chi-tieu | so-don
        CustomerTierClassifier classifier = customerService.classifier(tierKey);
        List<CustomerSummary> customers = customerService.summaries(classifier, q, sort);

        Map<CustomerTier, Long> tierCounts = new EnumMap<>(CustomerTier.class);
        for (CustomerTier t : CustomerTier.values()) {
            tierCounts.put(t, customers.stream().filter(c -> c.getTier() == t).count());
        }
        model.addAttribute("customers", customers);
        model.addAttribute("tierCounts", tierCounts);
        model.addAttribute("totalSpent", customers.stream().map(CustomerSummary::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("tierStrategies", classifier.getAll());
        model.addAttribute("currentTier", classifier.getCurrent());
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        return "admin/customers";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(value = "xepHang", required = false) String tierKey,
                         Model model) {
        User customer = customerService.getCustomer(id);
        CustomerTierClassifier classifier = customerService.classifier(tierKey);
        model.addAttribute("c", customerService.summary(customer, classifier));
        model.addAttribute("currentTier", classifier.getCurrent());
        model.addAttribute("orders", customerService.ordersOf(customer));
        model.addAttribute("reviews", reviewRepository.findByUserId(customer.getId()));
        return "admin/customer-detail";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id,
                         @RequestParam(value = "back", required = false) String back,
                         Principal principal, RedirectAttributes ra) {
        customerService.getCustomer(id);
        try {
            userService.toggleEnabled(id, principal.getName());
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản khách hàng");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "detail".equals(back) ? "redirect:/admin/customers/" + id : "redirect:/admin/customers";
    }
}
