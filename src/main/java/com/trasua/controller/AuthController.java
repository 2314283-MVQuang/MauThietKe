package com.trasua.controller;

import com.trasua.dto.RegisterForm;
import com.trasua.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final ObjectProvider<ClientRegistrationRepository> googleClients;

    public AuthController(UserService userService, ObjectProvider<ClientRegistrationRepository> googleClients) {
        this.userService = userService;
        this.googleClients = googleClients;
    }

    /** Có bật đăng nhập Google không (đã điền Client ID chưa) - dùng để hiển thị nút. */
    @ModelAttribute("googleEnabled")
    public boolean googleEnabled() {
        return googleClients.getIfAvailable() != null;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegisterForm form, Model model, RedirectAttributes ra) {
        try {
            userService.register(form);
            ra.addFlashAttribute("success", "Đăng ký thành công! Mời bạn đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
