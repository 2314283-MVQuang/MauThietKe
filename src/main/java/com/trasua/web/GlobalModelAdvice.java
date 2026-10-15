package com.trasua.web;

import com.trasua.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Dữ liệu dùng chung cho mọi trang (số món trong giỏ) và xử lý lỗi chung. */
@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
public class GlobalModelAdvice {

    private final Cart cart;

    public GlobalModelAdvice(Cart cart) {
        this.cart = cart;
    }

    /** Liên hệ của quán (application.properties: app.shop.phone / app.shop.email) - hiện ở chân trang. */
    @org.springframework.beans.factory.annotation.Value("${app.shop.phone:0333647940}")
    private String shopPhone;

    @org.springframework.beans.factory.annotation.Value("${app.shop.email:2314283@dlu.edu.vn}")
    private String shopEmail;

    @ModelAttribute("shopPhone")
    public String shopPhone() {
        return shopPhone;
    }

    @ModelAttribute("shopEmail")
    public String shopEmail() {
        return shopEmail;
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        return cart.getTotalQuantity();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Không tìm thấy");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
