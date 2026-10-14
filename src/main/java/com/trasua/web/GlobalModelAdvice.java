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
