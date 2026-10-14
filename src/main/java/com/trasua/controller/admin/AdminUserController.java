package com.trasua.controller.admin;

import com.trasua.dto.UserForm;
import com.trasua.entity.Role;
import com.trasua.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Quản lý tài khoản & phân quyền (chỉ ADMIN). */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/new")
    public String create(Model model) {
        return form(new UserForm(), model);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        return form(UserForm.from(userService.getById(id)), model);
    }

    private String form(UserForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") UserForm form, Principal principal,
                       Model model, RedirectAttributes ra) {
        try {
            userService.saveByAdmin(form, principal.getName());
            ra.addFlashAttribute("success", "Đã lưu tài khoản");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return form(form, model);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, Principal principal, RedirectAttributes ra) {
        try {
            userService.toggleEnabled(id, principal.getName());
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
