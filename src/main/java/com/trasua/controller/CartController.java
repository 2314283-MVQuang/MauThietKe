package com.trasua.controller;

import com.trasua.dto.CheckoutForm;
import com.trasua.entity.Order;
import com.trasua.entity.OrderType;
import com.trasua.entity.PaymentMethod;
import com.trasua.entity.Promotion;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.pattern.strategy.payment.PaymentProcessor;
import com.trasua.service.OrderService;
import com.trasua.service.PromotionService;
import com.trasua.service.UserService;
import com.trasua.web.Cart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final Cart cart;
    private final OrderService orderService;
    private final PromotionService promotionService;
    private final UserService userService;
    private final PaymentProcessor paymentProcessor;

    public CartController(Cart cart, OrderService orderService, PromotionService promotionService,
                          UserService userService, PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
        this.cart = cart;
        this.orderService = orderService;
        this.promotionService = promotionService;
        this.userService = userService;
    }

    @GetMapping
    public String view(Principal principal, Model model) {
        User user = userService.getByUsername(principal.getName());
        boolean atCounter = user.getRole() != Role.CUSTOMER;

        CheckoutForm form = new CheckoutForm();
        if (atCounter) {
            form.setOrderType(OrderType.TAI_QUAN);
        } else {
            form.setCustomerName(user.getFullName());
            form.setPhone(user.getPhone());
            form.setDeliveryAddress(user.getAddress());
        }
        model.addAttribute("form", form);
        fillModel(model, atCounter);
        return "cart/index";
    }

    private void fillModel(Model model, boolean atCounter) {
        model.addAttribute("cart", cart);
        model.addAttribute("price", orderService.previewCart(cart)); // STRATEGY tính giảm giá
        model.addAttribute("atCounter", atCounter);
        model.addAttribute("orderTypes", atCounter
                ? List.of(OrderType.TAI_QUAN, OrderType.MANG_DI, OrderType.GIAO_HANG)
                : List.of(OrderType.MANG_DI, OrderType.GIAO_HANG));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        // STRATEGY: mỗi chiến lược thanh toán tự cung cấp ảnh QR của nó
        model.addAttribute("paymentQrs", paymentProcessor.qrImages());
        model.addAttribute("promotions", promotionService.findUsable());
    }

    @PostMapping("/update")
    public String update(@RequestParam("index") int index, @RequestParam("quantity") int quantity) {
        cart.updateQuantity(index, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("index") int index) {
        cart.remove(index);
        return "redirect:/cart";
    }

    @PostMapping("/promo")
    public String applyPromotion(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "action", defaultValue = "apply") String action,
                                 RedirectAttributes ra) {
        if ("remove".equals(action)) {
            cart.setPromotionCode(null);
            ra.addFlashAttribute("success", "Đã bỏ mã khuyến mãi");
            return "redirect:/cart";
        }
        try {
            Promotion promotion = promotionService.getUsableByCode(code);
            cart.setPromotionCode(promotion.getCode());
            ra.addFlashAttribute("success", "Đã áp dụng mã " + promotion.getCode() + ": " + promotion.getName());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute("form") CheckoutForm form, Principal principal,
                           Model model, RedirectAttributes ra) {
        User user = userService.getByUsername(principal.getName());
        try {
            Order order = orderService.checkout(cart, form, user);
            ra.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn: " + order.getOrderCode());
            return "redirect:/orders/" + order.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            fillModel(model, user.getRole() != Role.CUSTOMER);
            return "cart/index";
        }
    }
}
