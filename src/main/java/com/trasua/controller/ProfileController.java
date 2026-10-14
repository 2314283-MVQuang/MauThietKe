package com.trasua.controller;

import com.trasua.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String profile(Principal principal, Model model) {
        model.addAttribute("user", userService.getByUsername(principal.getName()));
        return "profile";
    }

    @PostMapping
    public String update(@RequestParam("fullName") String fullName,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "email", required = false) String email,
                         @RequestParam(value = "address", required = false) String address,
                         Principal principal, RedirectAttributes ra) {
        try {
            userService.updateProfile(principal.getName(), fullName, phone, email, address);
            ra.addFlashAttribute("success", "Đã cập nhật thông tin");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal, RedirectAttributes ra) {
        try {
            userService.changePassword(principal.getName(), oldPassword, newPassword, confirmPassword);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}
